
//三个特性：
//1.不需要分号
//2.不需要写get,set方法
//3.最后一个值默认为返回值，不需要写return
//public class ProjectVersion{
//    private int major
//    private int minor
//
//    public ProjectVersion(int major , int minor){
//        this.major = major
//        this.minor = minor
//    }
//
//    int getMajor() {
//        major
//    }
//
//    void setMajor(int major) {
//        this.major = major
//    }
//
//}
//
//ProjectVersion v1 = new ProjectVersion(1,1);
//println v1.minor
//
//ProjectVersion v2 = null
//
//println v1 == v2

//groovy 高效特性
//1.可选的类型定义

//def version = 1
//
////2 assert
//
//assert version == 1
//
////3 括号是可选的
//println version
//
////4 字符串
//def s1 = 'cyjz'
//def s2 = "gradle version is ${version}"
//def s3 = '''my
// name is
// cyjz'''
//
//println s1
//println s2
//println s3

//特性5 集合api
//list
//def buildTool = ['ant','maven']
//buildTool << 'gradle'
//assert buildTool.getClass() == ArrayList
//println buildTool.size()
////map
//def buildYears = ['ant':2000,'maven':2004]
//println buildYears.ant
//println buildYears['maven']
//
//println buildYears.getClass()
////linked hash map
////6.闭包
//def c1 = {
//    v ->
//        print v
//
//}
//def c2 = {
//    print 'hello'
//}
//
//def method1(Closure closure){
//    closure('param')
//}
//
//def method2(Closure closure){
//    closure()
//}
//method1(c1)
//method2(c2)

//构建脚本中默认都是有个Project实例的
apply plugin:'java'

version = '0.1'

repositories{
    mavenCentral()
}

dependencies{
    compile 'commons-codec:commons-codec:1.6'
}