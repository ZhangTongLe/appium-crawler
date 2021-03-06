## Appium-crawler

### 工具介绍及背景
用于自动遍历app，简而言之就是有一定算法规则的app monkey测试工具，用于测试app稳定性测试

### 工具原理
首先通过配置文件引导app登陆到"首根页"面，然后解析出当前页面元素，获取每个元素的属性(id，text，class等等)，并准确计算出各个元素的xpath值，生成ElementNode对象加入到Queue里面，这边我用的是广度优先的算法，根据先进先出的原则挨个遍历元素，当对元素进行完action操纵之后，首先判断是否跳页，然后判断该页面是否之前进来过(否则不会解析当前页面各个元素，并将其push到queue当中去，防止重复push)，依次类推，如果当前元素不属于该页面，则会首先回退到首根页，然后往上追溯再计算出该元素的所有父节点，并加入到stack中，根据stack后进先出的特点，计算出从首根页进入到该元素时需要点击元素的轨迹。

![image](https://note.youdao.com/yws/public/resource/de1d669d836a750cc890606de77aeba2/xmlnote/02644B5C68224589B674973FAA134A45/4208)

### 备注
该工具还有很多不完善的地方，但基本算法逻辑已经实现，并已经初步稳定实现功能，下一步打算适配iOS版本的app遍历，并且action目前只追加了点击以及输入操作，后续会加上一些繁琐的，比如双击，手势滑动等等，以及还会加上app crash监听工具等等(目前仍然需要依赖app自身的第三方sdk来实现)
