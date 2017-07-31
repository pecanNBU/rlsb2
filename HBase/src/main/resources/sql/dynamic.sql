人脸动态库
------------------------------------------------------------------------------------
|                                   person                                          |
------------------------------------------------------------------------------------
|            |                           CF：i                                      |
------------------------------------------------------------------------------------
|   图片ID   |    图片    |    设备ID    |    描述信息    |    附加信息   |   时间  |
------------------------------------------------------------------------------------
|   rowkey   |      p     |     f        |      d         |       e       |    t    |
------------------------------------------------------------------------------------
create 'person',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 -------------------------------------------------------------
 
 人脸特征
-----------------------------
|         perFea            |
-----------------------------
|            |    CF：i     |
-----------------------------
|   图片ID   |    特征值    |
-----------------------------
|   rowkey   |      fea     |
-----------------------------

create 'perFea',
{NAME => 'f', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 -------------------------------------------------------------
 
 车辆动态库
------------------------------------------------------------------------------------------------
|                                   car                                                        |
------------------------------------------------------------------------------------------------
|            |                           CF：i                                                 |
------------------------------------------------------------------------------------------------
|   图片ID   |    图片    |    设备ID    |    描述信息   |    附加信息  |   车牌号  |   时间   |
------------------------------------------------------------------------------------------------
|   rowkey   |      p     |     f        |       d       |        e     |     n     |     t    |
------------------------------------------------------------------------------------------------

create 'car',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '524280',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
-------------------------------------------------------------
 
 车辆特征
-------------------------------------------
|                 carFea                  |
-------------------------------------------
|            |              CF：i         |
-------------------------------------------
|   图片ID   |    特征值    |    车牌号   |
-------------------------------------------
|   rowkey   |      fea     |       n     |
-------------------------------------------
 
 create 'carFea',
{NAME => 'f', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 应用平台上传的图片
 
 人脸图：
----------------------------------------
|             upPerFea                 |
----------------------------------------
|            |          CF：i          |
----------------------------------------
|   图片ID   |    小图    |    特征    |
----------------------------------------
|   rowkey   |      s     |     f      |
----------------------------------------

create 'upPerFea',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 车辆图：
------------------------------------------------------
|                       upCarFea                     |
------------------------------------------------------
|            |                 CF：i                 |
------------------------------------------------------
|   图片ID   |    小图    |    特征    |    车牌号   |
------------------------------------------------------
|   rowkey   |      s     |     f      |       n     |
------------------------------------------------------

create 'upCarFea',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}
 
 查询结果：
------------------------------------------------------
|                       searchRes                    |
------------------------------------------------------
|            |                 CF：i                 |
------------------------------------------------------
|   图片ID   | 查询图片ID | 返回图片Id |    车牌号   |
------------------------------------------------------
|   rowkey   |      q     |     r      |      si     |
------------------------------------------------------

create 'searchRes',
{NAME => 'i', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'snappy', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65535',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}