# Java SDK for Hyperledger Fabric 1.0

**SDK作用和目的**

我的理解就是SDK是为了用java程序来管理fabric的链以及部署、执行用户的chaincode的一套工具包，
类似fabric中的app作用。

**fabric-sdk-java变化**

1._chaincode接口的变化_
跟0.6相比，现在的chaincode简介了很多。主要是table及相关的操作被删除，这也是考虑到KV数据库转换成table形式后数据量增大，
没有索引的情况下会严重影响到查询速度。保留了KV及KV相关操作map的接口,还保留了以前的调用其他chaincode方法,
以及验证Signature方法。对于如何实现调用chaincode,暂时没有给出例子,我看很多相关的方法还没有像0.6那么完善。后文有具体
介绍0.6如何管理chaincode。

**fabric-sdk-java代码结构调整**

总的方向将之前耦合度比较高的地方进行解耦。如将安全级别、协议等待时间等一系列配置相关的东西单独建了一个类Config来存取和设置，
这样的好处是可以进行统一管理，特别是对于维护人员来说，只需要改这一个地方就行了，增强了可维护性，0.6里面是零零散散的分配在各个代码中。
再比如，现在将chaincodeID/chaincodeEndorsement都分出来作为处理chaincode和endorsement。由于这次fabric的变动较大，java肯定
还有很多变化我没察觉到，或者还有没跟上fabric的地方。


 
**0.6管理chaincode**
 
1）首先还是要新建一个链，然后给它建立memberservice连接，指定其地址、端口7054，并输入pem,目前0.6版本pem为空。
1.0则规划在sdk项目用专门的配置文件来存放pem文件，具体是放在cacerts目录下,但是这块代码还没有实现，还不需要用到.pem文件。
但1.0的fabric中要求instantiate一个chaincode的时候指名pem文件的路径

2）给新建链设定一个本地存储的KV文件,我的理解是它的作用类似于fabric中的rocksdb。
SDK说明文档提出不为客户端应用程序提供持久性方法，这一块值得完善。比如chaincode执行的操作日志等，可以用MySQL数据库存储。

3)给新建链连上peer，7051端口，pem还是为空，目前1.0没有让输入pem文件，应该后续有其他方式来取。fabric是需要指定其路径的

4)enroll一个用户，如果该用户没有register

5)连上事件库，7053端口，目前0.6版本只支持了block事件，接口中的卸载事件及Chaincode事件未实现。现在1.0还没有实现chaincode的事件，
还只看到Peer的事件，而且也还只有block事件的监听。这个我已经提出需求给sdk的开发人员，后续这里应该会满足需求。

6)然后这里提供了一个用来设定chaincode路径、参数、enrollName、ChaincodeLanguage,
invoke/query都是并交给Member类去deploy/invoke/query,TransactionContext类接过deploy/invoke/query后
最后给Fabric类的编译文件是真正完成的。目前1.0中TransactionContext没见到处理chaincode中的deploy/invoke/query
这也是为什么没有管理chaincode例子的直接原因吧。

#  Hyperledger Fabric 1.0

**chaincode变化**
   
原来的peer下的chaincode操作deploy被差分成了install和instantiate
install简单的指定order地址及端口号 chaincode名字 版本号 及路径，现在的路径是直接从github开始，用绝对路径会报错
instantiate也要指定order地址及端口、指明TLS是否打开、 CAfile路径、channel名字、chaincode名字、版本号 路径及参数 
invoke操作和instantiate类似，query则相对简单，而且invoke和query都用-C/-n代替了以前的 -n+hash（ChaincodeID）

这里channel的指定非常重要，文档中说明『If the called chaincode is on a different channel, only the Response 
is returned to the caller; any PutState calls will not have any effect on the ledger of the channel;
 effectively it is a `Query`』
我的理解是，就好比我们的数据库事物特性中的隔离性，这里对更改账本的操作进行严格的隔离，而对查询操作不进行隔离。
当然这会导致查询到的信息并不是最新的，因为很可能查询的同时，在进行更改操作。对于更改操作则必须指定对应的channel。

**JAVAchaincode变化**
现阶段java的Chaincode还不完善，很多方法还保留着以前老的，比如ChaincodeBase里面就是以前老的代码。最新的代码还是要看SDK
