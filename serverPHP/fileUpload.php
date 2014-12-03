<?php
/**
 *  本代码用于测试webuploader相关特性，存在很大的优化空间，不建议用于正式项目。
*/

/**
 *  PHP通用文件上传类
 *
 *  支持单文件和多文件上传
*/

class FileUpload{

    //要配置的内容
    private $path = "../uploads";
    private $allowtype = array('jpg', 'gif', 'png', 'mp4', 'mp3');
    private $maxsize = 99999999999;
    private $israndname = true;

    private $originName;
    private $tmpFileName;
    private $fileType;
    private $fileSize;
    private $newFileName;
    private $errorNum = 0;
    private $errorMess = "";
	
	private $isChunk = false;
	private $indexOfChunk = 0;

    /**
     * 用于设置成员属性($path, $allowtype, $maxsize, $israndname)
     * 可以通过连贯操作一次设置多个属性值
     * @param $key  成员属性（不区分大小写）
     * @param $val  为成员属性设置的值
     * @return object 返回自己对象$this, 可以用于连贯操作
     */
    function set($key, $val){
        $key = strtolower($key);
        if (array_key_exists($key, get_class_vars(get_class($this)))){
            $this->setOption($key, $val);
        }
        return $this;
    }

    /**
     * 调用该方法上传文件
     * Enter description here ...
     * @param $fileField    上传文件的表单名称
     */
    function upload($fileField, $info){
		
		//判断是否为分块上传
		$this->checkChunk($info);
		
        if (!$this->checkFilePath($this->path)){
            $this->errorMess = $this->getError();
            return false;
        }

        //将文件上传的信息取出赋给变量
        $name = $_FILES[$fileField]['name'];
        $tmp_name = $_FILES[$fileField]['tmp_name'];
        $size = $_FILES[$fileField]['size'];
        $error = $_FILES[$fileField]['error'];

        //设置文件信息
        if ($this->setFiles($name, $tmp_name, $size, $error)){
        
			//如果是分块，则创建一个唯一名称的文件夹用来保存该文件的所有分块
			if($this->isChunk){
                $uploadDir = $this->path;
                $tmpName = $this->setDirNameForChunks($info);
                if(!$this->checkFilePath($uploadDir . '/' . $tmpName)){
				    $this->errorMess = $this->getError();
            	    return false;
                }

                //创建一个对应的文件，用来记录上传分块文件的修改时间，用于清理长期未完成的垃圾分块
                touch($uploadDir.'/'.$tmpName.'.tmp');
			}

            if($this->checkFileSize() && $this->checkFileType()){
                $this->setNewFileName();
                if ($this->copyFile()){
                    return $this->newFileName;
                }
            }
        }

        $this->errorMess = $this->getError();
        return false;
    }

    public function chunksMerge($uniqueFileName, $chunksTotal, $fileExt){

        $targetDir = $this->path.'/'.$uniqueFileName;

        //检查对应文件夹中的分块文件数量是否和总数保持一致
        if($chunksTotal > 1 && (count(scandir($targetDir)) - 2) == $chunksTotal){
            //同步锁机制
            $lockFd = fopen($this->path.'/'.$uniqueFileName.'.lock', "w");
            if(!flock($lockFd, LOCK_EX | LOCK_NB)){
                fclose($lockFd);
                return false;
            }

            //进行合并
            $this->fileType = $fileExt;
            $finalName = $this->path.'/'.($this->setOption('newFileName', $this->proRandName()));
            $file = fopen($finalName, 'wb');
            for($index = 0; $index < $chunksTotal; $index++){
                $tmpFile = $targetDir.'/'.$index;
                $chunkFile = fopen($tmpFile, 'rb');
                $content = fread($chunkFile, filesize($tmpFile));
                fclose($chunkFile);
                fwrite($file, $content);

                //删除chunk文件
                unlink($tmpFile);
            }

            fclose($file);

            //删除chunk文件夹
            rmdir($targetDir);
            unlink($this->path.'/'.$uniqueFileName.'.tmp');

            //解锁
            flock($lockFd, LOCK_UN);
            fclose($lockFd);
            unlink($this->path.'/'.$uniqueFileName.'.lock');

            return $this->newFileName;

        }
        return false;
    }

    //获取上传后的文件名称
    public function getFileName(){
        return $this->newFileName;
    }

    //上传失败后，调用该方法则返回，上传出错信息
    public function getErrorMsg(){
        return $this->errorMess;
    }

    //设置上传出错信息
    public function getError(){
        $str = "上传文件<font color='red'>{$this->originName}</font>时出错：";
        switch ($this->errorNum) {
            case 4:
                $str.= "没有文件被上传";
                break;
            case 3:
                $str.= "文件只有部分被上传";
                break;
            case 2:
                $str.= "上传文件的大小超过了HTML表单中MAX_FILE_SIZE选项指定的值";
                break;
            case 1:
                $str.= "上传的文件超过了php.ini中upload_max_filesize选项限制的值";
                break;
            case -1:
                $str.= "未允许的类型";
                break;
            case -2:
                $str.= "文件过大， 上传的文件夹不能超过{$this->maxsize}个字节";
                break;
            case -3:
                $str.= "上传失败";
                break;
            case -4:
                $str.= "建立存放上传文件目录失败，请重新指定上传目录";
                break;
            case -5:
                $str.= "必须指定上传文件的路径";
                break;

            default:
                $str .= "未知错误";
        }
        return $str."<br>";
    }

	//根据文件的相关信息为分块数据创建文件夹
	//md5(当前登录用户的数据库id + 文件原始名称 + 文件类型 + 文件最后修改时间 + 文件总大小)
	private function setDirNameForChunks($info){
		$str = ''.$info['userId'] . $info['name'] . $info['type'] . $info['lastModifiedDate'] . $info['size'];
		return md5($str);
	}

    //设置和$_FILES有关的内容
    private function setFiles($name="", $tmp_name="", $size=0, $error=0){
        $this->setOption('errorNum', $error);
        if ($error) {
            return false;
        }
        $this->setOption('originName', $name);
        $this->setOption('tmpFileName', $tmp_name);
        $aryStr = explode(".", $name);
        $this->setOption("fileType", strtolower($aryStr[count($aryStr)-1]));
        $this->setOption("fileSize", $size);
        return true;
    }

	private function checkChunk($info){
		if(isset($info['chunks']) && $info['chunks'] > 0){
			$this->setOption("isChunk", true);
			
			if(isset($info['chunk']) && $info['chunk'] >= 0){
				$this->setOption("indexOfChunk", $info['chunk']);
				
				return true;
			}
			
			throw new Exception('分块索引不合法');
		}
		
		return false;
	}

    //为单个成员属性设置值
    private function setOption($key, $val){
        $this->$key = $val;
        return $val;
    }

    //设置上传后的文件名称
    private function setNewFileName(){
   		if($this->isChunk){		//如果是分块，则以分块的索引作为文件名称保存
    		$this->setOption('newFileName', $this->indexOfChunk);
   		}elseif($this->israndname) {
            $this->setOption('newFileName', $this->proRandName());
        }else{
            $this->setOption('newFileName', $this->originName);
        }
    }

    //检查上传的文件是否是合法的类型
    private function checkFileType(){
        if (in_array(strtolower($this->fileType), $this->allowtype)) {
            return true;
        }else{
            $this->setOption('errorNum', -1);
            return false;
        }
    }


    //检查上传的文件是否是允许的大小
    private function checkFileSize(){
        if ($this->fileSize > $this->maxsize) {
            $this->setOption('errorNum', -5);
            return false;
        }else{
            return true;
        }
    }

    //检查是否有存放上传文件的目录
    private function checkFilePath($target){
    	
		if (empty($target)) {
            $this->setOption('errorNum', -5);
            return false;
        }
		
        if (!file_exists($target) || !is_writable($target)) {
            if (!@mkdir($target, 0755)) {
                $this->setOption('errorNum', -4);
                return false;
            }
        }

        $this->path = $target;
        return true;
    }

    //设置随机文件名
    private function proRandName(){
        $fileName = date('YmdHis')."_".rand(100,999);
        return $fileName.'.'.$this->fileType;
    }

    //复制上传文件到指定的位置
    private function copyFile(){
        if (!$this->errorNum) {
            $path = rtrim($this->path, '/').'/';
            $path.= $this->newFileName;
            if (@move_uploaded_file($this->tmpFileName, $path)) {
                return true;
            }else{
                $this->setOption('errorNum', -3);
                return false;
            }
        }else{
            return false;
        }
    }
}

//关闭缓存
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");

$uploader = new FileUpload();

//用于断点续传，验证指定分块是否已经存在，避免重复上传
if(isset($_POST['status'])){
    if($_POST['status'] == 'chunkCheck'){
        $target = '../uploads/'.$_POST['name'].'/'.$_POST['chunkIndex'];
        if(file_exists($target) && filesize($target) == $_POST['size']){
            die('{"ifExist":1}');
        }
        die('{"ifExist":0}');

    }elseif($_POST['status'] == 'md5Check'){

        //todo 模拟持久层查询
        $dataArr = array(
            'b0201e4d41b2eeefc7d3d355a44c6f5a' => 'kazaff2.jpg'
        );

        if(isset($dataArr[$_POST['md5']])){
            die('{"ifExist":1, "path":"'.$dataArr[$_POST['md5']].'"}');
        }
        die('{"ifExist":0}');
    }elseif($_POST['status'] == 'chunksMerge'){

    	if($path = $uploader->chunksMerge($_POST['name'], $_POST['chunks'], $_POST['ext'])){
            //todo 把md5签名存入持久层，供未来的秒传验证
    		die('{"status":1, "path": "'.$path.'"}');
    	}
    	die('{"status":0');
    }
}

if(($path = $uploader->upload('file', $_POST)) !== false){
    die('{"status":1, "path": "'.$path.'"}');
}
die('{"status":0}');

