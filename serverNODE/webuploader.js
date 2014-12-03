/**
 * Created by kazaff on 2014/11/13.
 */

var crypto = require("crypto"),
    uuid = require("node-uuid"),
    async = require("async"),
    path = require("path"),
    fs = require("fs");

module.exports = {
    createUniqueFileName: function(info){
        //md5(当前登录用户的数据库id + 文件原始名称 + 文件类型 + 文件最后修改时间 + 文件总大小)
        var str = "" + info.userId + info.name + info.type + info.lastModifiedDate + info.size;
        return crypto.createHash("md5").update(str, "utf8").digest("hex");
    },
    randomFileName: function(extname){
        return uuid.v4() + "." +extname;
    },
    chunksMerge: function(dir, targetSteam, total, callback){

        var index = 0;
        async.whilst(function(){
            return index < total;
        }, function(cb){

            var originStream = fs.createReadStream(path.join(dir, index.toString()));
            originStream.pipe(targetSteam, {end: false});
            originStream.on("end", function(){
                //删除文件
                fs.unlink(path.join(dir, index.toString()), function(err){

                    if(err){
                        //todo
                        console.error(err);
                    }
                });

                index++;
                cb();
            });

        }, function(err){

            callback(err);
        });

    }
};