说明
---

因为原始webuploader.js不支持为formData设置函数类型参数，这将导致不能在控件初始化后修改该参数，所以修改了对应源码，大概在webuploader.js源码的[3170](https://github.com/fex-team/webuploader/blob/master/dist/webuploader.js#L3170)行。

具体细节请参看[这里](http://blog.kazaff.me/2014/11/14/%E8%81%8A%E8%81%8A%E5%A4%A7%E6%96%87%E4%BB%B6%E4%B8%8A%E4%BC%A0/)。
