### 前言
该设计模式系列为阅读《Android源码设计模式解析与实战》一书过程中的一些笔记，以及个人的理解。只是想把设计模式理解的更加透彻，说的更加简单。当然，说的好，不如做的好。学习后，也要尽量学会在项目中实战。设计模式，不是把一件事情弄的更复杂，而是弄的更加简单。我认为，这是设计模式好坏的标准。有许多理解不到位的地方，望朋友们多多指教。
### 定义
将一个复杂对象的构造过程分离，使得同样的构造过程可以创建不同的表示；
### 个人理解
TMD就像同样是一场考试，考试是一个过程，全班55个人做会交出55张不同的考卷，秒懂了吧！
### 代码示例
    // 考卷 抽象类 角色 product
    abstract class ExaminationPaper {
        private String tianKongTi;
        private String zuoWenTi;

        protect ExaminationPaper(){
        }
        //作答填空题
        public void setTianKong(String tianKongAnswer){
            tianKongTi=tianKongAnswer;
        }
        //作答作文题
        public abstract void setZuoWent();

    }
    // 小明的考卷 考卷的实体类
    class XiaoMing'Paper extends ExaminationPaper{
        public void setZuoWen(){
            zuoWenTi="大家好，我是小明，我一定要拿第一名";
        }
    }

    // 55个同学 抽象builder
    abstract class ExaminationBuilder{
        public abstarct void tianKongBuilder(String tianKongAnswer);
        public abstarct void zuoWenBuilder();
        public abstarct ExaminationPaper create();
    }
    // 小明这位同学  实体
    class xiaoMingBuilder extends ExaminationBuilder{
        private ExaminationPaper paper;

        public xiaoMingBuilder(){
            paper=new XiaoMing'Paper();
        }

        public xiaoMingBuilder tianKongBuilder(String tianKongAnswer){
            paper.setTianKong(tianKongAnswer);
        }
        public xiaoMingBuilder zuoWenBuilder(){
            paper.setZuoWen();
        }
        public ExaminationPaper ExaminationPaper create(){
            return paper;
        }
    }
    // Main
    class Main{
        void main(){
            ExaminationBuilder builder =new xiaoMingBuilder();
            builder.tianKongBuilder("小明做的填空题")
        }
    }
### 分析
这个适用于“产品”这类对象的创建，有上面可知，假如我们将考卷作为一种产品，首先考卷是实现把题目出好了的，不同的学生拿到的考卷是一样的。所以，我们把这些抽象出来。但是呢？真正考试给不同的学生做的时候就会有不同的结果；所以，每个不同的学生会对“考卷”这款产品进行一次深加工，我们建立起不同的产品模型；
同样，作为对产品深加工的同学们都会要干同样的事情，就是给每个题目作答，然后交出试卷。我们首先，把要做题的这一群同学抽象出来。再给每个同学写一个实现体。这样我们做卷开始啦，首先new一个人，然后让这个同学输入答案就好。

### 小结

我们可以从工业生产的角度出发，那么builder模式我们可以把它认为是一个手工作坊，或者说就是一种个性化定制工厂。原料是一致的，通过加工，我么能够得到不同用户个性定制化的产品。比如文化衫，我们可以把空白的文化衫抽离出来，
并面对不同个性化的要求产出不同的产品。记住，文化衫是我们的product，而用户是我们的builder。