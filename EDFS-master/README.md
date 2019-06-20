# EDFS:基于微服务架构的分布式文件系统

## 使用说明
### NameNode的启动
控制台进入NameNode目录执行：mvn spring-boot:run

参数的配置：在application.properties中修改默认参数

block.default-size=1000

block.default-replicas=2

### DataNode的启动
进入DataNode目录

mvn spring-boot:run -Dserver.port=8008

mvn spring-boot:run -Dserver.port=8009

### 模拟请求
因为没有做前端，所以使用了POSTMAN软件来发送

一共四种指令：

GET /AllFile - 列出文件系统/目录内容

GET /a.txt - 下载a.txt文件（不限于txt）

POST /a.txt - 上传a.txt文件

DEL /a.txt - 删除a.txt文件

## 实现说明
### DataNode
DadaNode因为只负责存储和提供数据，所以只需要和NameNode交互即可，所以只有三个接口负责存文件，传文件，删文件

DataNode默认存取文件位置

### NameNode
NameNode作为负责所有业务逻辑的节点，维护了两个数据结构，FileSystem 和 DataNodeManager

### 功能实现

#### 上传文件

首先NameNode的临时文件存取路径：
```
public String nameNodeTempDir="F:/DFSData/nameNodeTemp/";
```
请保证此目录存在，（若不存在会出错）

接受到上传的文件后会在NameNode分块，并封装成文件保存在上述目录中

***
#### 下载文件

这一部分是比较头疼的部分，首先NameNode从FileSystem中查询所需文件的各个块分布在哪些DataNode上，查询后向DataNode发送请求，DataNode收到请求后将文件传给NameNode，NameNode先将文件块存在本地临时目录下，然后合并各个块成为完成的文件。

***
#### 删除文件

比较简单，不赘述


