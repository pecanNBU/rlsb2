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

create 'device',
{NAME => 'adn', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESSION =>
'NONE', VERSIONS => '1', MIN_VERSIONS => '0', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536',
 IN_MEMORY => 'true', BLOCKCACHE => 'true'}