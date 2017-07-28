人脸动态库
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |  RowKey    |
-------------------------
| 设备ID   |     f      |
-------------------------
|  图片    |     p      |
-------------------------
| 描述信息 |     d      |
-------------------------
| 附加信息 |     e      |
-------------------------
| 时间     |     t      |
-------------------------


create 'person',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 -------------------------------------------------------------
 
 人脸特征
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  特征    |   fea      |
-------------------------

create 'perFea',
{NAME => 'f', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 -------------------------------------------------------------
 
 车辆动态库
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
| 设备ID   |     f      |
-------------------------
|  图片    |     p      |
-------------------------
| 描述信息 |     d      |
-------------------------
| 附加信息 |     e      |
-------------------------
| 车牌号   |     n      |
-------------------------
| 时间     |     t      |
-------------------------

create 'car',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
-------------------------------------------------------------
 
 车辆特征
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  特征    |   fea      |
-------------------------
 
 create 'carFea',
{NAME => 'f', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 应用平台上传的图片
 
 人脸图：
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  小图    |   s        |
-------------------------
|  特征    |   f        |
-------------------------

create 'upPerFea',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 车辆图：
-------------------------
|   name   |   field    |
-------------------------
|  图片ID  |   RowKey   |
-------------------------
|  小图    |   s        |
-------------------------
|  特征    |   f        |
-------------------------
|  车牌号  |   n        |
-------------------------

create 'upCarFea',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 查询结果：
 -------------------------
|   name   |   field    |
-------------------------
|  查询ID  |   RowKey   |
-------------------------
|查询图片ID|     q      |
-------------------------
|返回图片Id|     r      |
-------------------------
| 相似度   |     si     |
-------------------------

create 'searchRes',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}