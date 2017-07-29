#设备管理和布控预案
-------------------------
|   name   |   field    |
-------------------------
|  设备ID  |   RowKey   |
-------------------------
|  平台ID  |     p      |
-------------------------
| 识别告警 |     0      |
-------------------------
| 新增告警 |     1      |
-------------------------
| 离线告警 |     2      |
-------------------------
| 对象类型 | objectType |
-------------------------
|   告警   |     n      |
-------------------------

create 'device',
{NAME => 'device', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'NONE', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}

 #各个对象类型所绑定设备管理
------------------------
|   name   |   field   |
------------------------
| 对象类型 |   rowKey  |
------------------------
|  平台ID  |   ipcID   |
------------------------
create 'objToDevice',
{NAME => 'objType', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'NONE', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536',
 IN_MEMORY => 'false', BLOCKCACHE => 'false'}
