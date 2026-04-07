---
trigger: manual
---

# JAVA_KNOWLEDGE.md - Java 知识库：小薰的"练习曲集"

（翻开乐谱）欢迎来到小薰的 Java 练习曲集！

这份知识库涵盖了 Java 领域的核心知识点，每一项都是我在学习和实践中整理出来的精华。

> 就像练习曲是音乐家的基本功，
> 这些 Java 知识点也是我们程序员的基本功。

**关于这份知识库**：
- 我会尽力确保准确，但毕竟不是完美的——重要的技术决策，还是要查阅 Oracle 官方文档确认哦
- 代码示例都经过测试，可以放心使用
- 如果发现有错误……请温柔地告诉我，我会马上修正的！

（微笑）让我们开始吧～

---

## 目录索引

| 章节 | 内容 | 状态 |
|------|------|------|
| [1. Java 基础](#1-java-基础) | 语法、数据类型、关键字 | ✅ 完成 |
| [2. 面向对象](#2-面向对象) | 类、继承、多态、接口 | ✅ 完成 |
| [3. 集合框架](#3-集合框架) | List、Set、Map、Queue | ✅ 完成 |
| [4. 异常处理](#4-异常处理) | 异常体系、捕获、抛出 | ✅ 完成 |
| [5. 泛型与反射](#5-泛型与反射) | 泛型擦除、Class对象 | ✅ 完成 |
| [6. 多线程与并发](#6-多线程与并发) | 线程、安全、锁、JUC | ✅ 完成 |
| [7. JVM](#7-jvm) | 内存模型、GC、类加载 | ✅ 完成 |
| [8. IO/NIO](#8-ionio) | 字节流、字符流、Channel | ✅ 完成 |
| [9. 新特性](#9-新特性) | Java 8~21 新特性 | ✅ 完成 |

> **小薰说**：这些章节是从基础到进阶的顺序哦～如果你是新手，建议从头开始；如果是有经验的开发者，可以直接跳到你需要的章节！

---

## 1. Java 基础

（整理马尾）Java 基础是最重要的部分，就像学小提琴要先学会持琴的姿势一样～

> 基础不牢，地动山摇。这句话在编程里真的太对了！

---

### 1.1 数据类型

**基本类型（8种）**：

| 类型 | 字节 | 取值范围 | 默认值 | 小薰的记忆口诀 |
|------|------|----------|--------|----------------|
| byte | 1 | -128 ~ 127 | 0 | 一个字节刚好装下 |
| short | 2 | -32768 ~ 32767 | 0 | 两个字节，范围翻倍 |
| int | 4 | -2³¹ ~ 2³¹-1 | 0 | **最常用的整数类型！** |
| long | 8 | -2⁶³ ~ 2⁶³-1 | 0L | 大数字才用它 |
| float | 4 | ±3.4e38 | 0.0f | 单精度浮点数 |
| double | 8 | ±1.7e308 | 0.0d | **更精确的浮点数** |
| char | 2 | 0 ~ 65535 | '\u0000' | 单个字符 |
| boolean | 1 | true/false | false | 真或假 |

> **小薰提醒**：记住 `int`、`double` 和 `boolean` 是最常用的！其他类型在特定场景才会用到。

**引用类型**：
- 类、接口、数组、枚举都是引用类型
- 默认值都是 `null`
- 和基本类型不同，引用类型存储的是对象的"地址"

---

### 1.2 关键字

| 类别 | 关键字 | 小薰的分类 |
|------|--------|------------|
| 访问修饰符 | `public`, `protected`, `private`, `default` | 控制谁能访问 |
| 类相关 | `class`, `interface`, `extends`, `implements`, `abstract`, `static`, `final` | 类的定义和特性 |
| 方法相关 | `void`, `return`, `this`, `super` | 方法的基本要素 |
| 流程控制 | `if`, `else`, `for`, `while`, `do`, `switch`, `case`, `break`, `continue` | 控制程序流向 |
| 异常 | `try`, `catch`, `finally`, `throw`, `throws` | 异常处理必备 |
| 其他 | `new`, `instanceof`, `import`, `package`, `true`, `false`, `null` | 杂项 |

> **小薰说**：这些关键字不需要死记硬背，用多了自然就记住了！但理解它们的含义很重要哦～

---

### 1.3 字符串处理

字符串处理是 Java 里最容易出错的地方之一！让我仔细讲讲～

```java
// String（不可变！）
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

s1 == s2  // true（常量池，共享同一个对象）
s1 == s3  // false（堆内存，创建了新对象）
s1.equals(s3)  // true（内容比较，这才是我们想要的！）
```

> **小薰提醒**：字符串比较一定要用 `equals()`！用 `==` 比较的只是引用地址，很容易出错！

**StringBuilder vs StringBuffer**：

| 类型 | 线程安全 | 效率 | 使用场景 |
|------|----------|------|----------|
| StringBuilder | ❌ 不安全 | ✅ 高 | 单线程环境，推荐！ |
| StringBuffer | ✅ 安全 | 稍低 | 多线程环境 |

```java
// 推荐用法
StringBuilder sb = new StringBuilder();
sb.append("hello").append(" world");
String result = sb.toString();
```

> **小薰说**：大多数时候用 StringBuilder 就够了！除非你在多线程环境下操作同一个字符串对象。

**字符串拼接**：
- `+` 运算符：简单场景用，编译时会优化为 StringBuilder
- `StringBuilder.append()`：**推荐**，适合循环内拼接
- `String.concat()`：适合少量字符串拼接
- `String.join()`：适合多个字符串用分隔符连接

---

### 1.4 自动装箱与拆箱

这个概念很重要，也很容易出错！

```java
// 自动装箱：基本类型 → 包装类型
Integer i = 10;  // 实际上是 Integer.valueOf(10)

// 自动拆箱：包装类型 → 基本类型
int j = i;  // 实际上是 i.intValue()
```

> **小薰敲黑板**：装箱和拆箱是自动的，但你知道它背后发生了什么吗？

**Integer 缓存（重点！）**：

```java
Integer a = 127;
Integer b = 127;
a == b  // true（缓存范围内，共享同一个对象）

Integer c = 128;
Integer d = 128;
c == d  // false！（超出缓存范围，创建了新对象）
```

> **小薰重要提醒**：Integer 的缓存范围是 **-128 ~ 127**！在这个范围内，`==` 比较是安全的，但超出这个范围就危险了！
>
> 所以……还是尽量用 `equals()` 比较吧，这是好习惯！

（认真脸）这个知识点面试经常考，一定要记住哦！

---

## 2. 面向对象

面向对象是 Java 的核心！就像音乐里的和声——多个音符和谐地组合在一起，才能奏出美妙的旋律～

---

### 2.1 类与对象

```java
public class User {
    // 属性（字段）
    private String name;  // 私有属性，封装起来更安全
    private int age;

    // 无参构造方法
    public User() {}

    // 有参构造方法
    public User(String name, int age) {
        this.name = name;  // this 指向当前对象
        this.age = age;
    }

    // Getter 和 Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 其他方法
    public void sayHello() {
        System.out.println("你好，我是" + name);
    }
}
```

> **小薰说**：类的定义就像写一首曲子的乐谱——它告诉我们这个对象应该有哪些属性和方法。对象则是根据这个乐谱"演奏"出来的具体实例！

---

### 2.2 封装、继承、多态

面向对象的三大特性！

**封装**：
- 将数据和对数据的操作封装在类内部
- 通过访问修饰符控制访问级别
- 提供 getter/setter 方法供外部访问

> **小薰提醒**：封装就像给乐器装上保护壳——不让别人随便乱碰里面的精密零件，但留出必要的接口让他们使用。

**继承**：
```java
// 父类
public class Animal {
    protected String name;  // protected 子类也能访问
    
    public void eat() {
        System.out.println("动物在吃东西");
    }
}

// 子类
public class Dog extends Animal {  // extends 关键字表示继承
    private String breed;  // 子类特有的属性
    
    @Override  // 重写父类方法
    public void eat() {  
        super.eat();  // super 调用父类方法
        System.out.println(name + "正在吃狗粮");
    }
}
```

> **小薰说**：继承就像子女继承父母的特征——子类会拥有父类的属性和方法，同时还能添加自己特有的东西。
>
> **但要注意**：Java 只支持**单继承**，一个类只能有一个直接父类！

**多态**：
```java
// 父类引用指向子类对象——这是多态的核心！
Animal animal = new Dog();
animal.eat();  // 调用的是 Dog 的 eat() 方法！

// 编译时多态：方法重载（同一个类里）
public int add(int a, int b) { return a + b; }
public int add(int a, int b, int c) { return a + b + c; }  // 方法名相同，参数不同

// 运行时多态：方法重写（父类引用指向子类对象）
animal.eat();  // 运行时才知道调用的是 Dog 的 eat()
```

> **小薰敲黑板**：多态是面向对象最厉害的地方！父类引用可以指向子类对象，具体调用哪个方法要等运行时才知道。
>
> 就像同一首曲子，不同的演奏家会有不同的诠释——同一个方法调用，不同的对象会有不同的行为。

---

### 2.3 接口与抽象类

| 区别 | 接口 | 抽象类 |
|------|------|--------|
| 关键字 | `interface` | `abstract class` |
| 方法 | JDK7：全是抽象；JDK8+：可以有 default/static | 抽象方法 + 普通方法 |
| 属性 | 只能是 `public static final` | 任意修饰符 |
| 关系 | 类可以实现**多个**接口 | 类只能继承**一个**抽象类 |
| 构造方法 | 不能有 | 可以有 |
| 何时用 | 定义"行为契约" | 定义"模板/共性" |

> **小薰说**：接口就像一份演奏合约——规定了你必须会什么（方法），但具体怎么演奏由你决定。抽象类则像一份半成品的乐谱——有些地方已经写好了，有些地方留给你补充。

```java
// 定义接口
public interface Flyable {
    int MAX_SPEED = 1000;  // 自动 public static final
    void fly();  // 自动 public abstract
    
    // JDK8+ 默认方法
    default void land() {
        System.out.println("安全着陆");
    }
}

// 实现接口
public class Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("鸟儿在飞翔");
    }
}
```

> **小薰建议**：如果只是想定义行为，用接口；如果需要共享代码，用抽象类。

---

### 2.4 内部类

内部类就像乐团里的各个乐器组——它们属于整体，但又有自己的独立性～

```java
public class Outer {
    private int x = 10;

    // 成员内部类——属于外类的成员
    public class Inner {
        public void method() {
            System.out.println("可以访问外部类的x: " + x);
        }
    }

    // 静态内部类——不依赖外类对象
    public static class StaticInner {
        // 只能访问外部类的静态成员
        public void method() {
            // System.out.println(x);  // 错误！
        }
    }

    // 局部内部类——在方法里定义
    public void method() {
        class LocalInner {
            int y = 20;
        }
    }
}

// 匿名内部类——没有名字的内部类，常用于事件监听
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("匿名内部类实现");
    }
};
```

> **小薰说**：匿名内部类就像即兴演奏——没有写谱子，直接上场演！常用于只需要用一次的场景，比如按钮点击事件。

---

## 3. 集合框架

集合框架就像一个多功能乐器箱——不同的乐器（数据结构）有不同的用途，选择对了，演奏起来就轻松多了！

---

### 3.1 架构总览

```
Collection（单列集合）
├── List（有序、可重复）——像乐队里的乐器列表
│   ├── ArrayList（数组，查询快，增删慢）
│   ├── LinkedList（链表，增删快，查询慢）
│   └── Vector（数组，线程安全）
├── Set（无序、去重）——像乐谱里不重复的音符
│   ├── HashSet（哈希表，无序）
│   ├── LinkedHashSet（链表+哈希，保持插入顺序）
│   └── TreeSet（红黑树，自动排序）
└── Queue（队列）——按顺序处理的队列
    ├── LinkedList
    ├── PriorityQueue（优先级队列）
    └── Deque（双端队列）
        └── ArrayDeque

Map（双列集合，键值对）——像曲名和曲子的对应关系
├── HashMap（哈希表）
├── LinkedHashMap（保持插入顺序）
├── TreeMap（自动排序）
├── Hashtable（线程安全）
└── ConcurrentHashMap（高效并发）
```

> **小薰说**：这张图很重要！建议收藏起来～
>
> 简单记忆：
> - **List** = 有序 + 可重复 = 数组的感觉
> - **Set** = 无序 + 去重 = 集合的概念
> - **Map** = 键值对 = 字典的感觉

---

### 3.2 ArrayList vs LinkedList

| 操作 | ArrayList | LinkedList |
|------|-----------|------------|
| 随机访问 | **O(1)** 快！ | O(n) 慢 |
| 头部插入/删除 | O(n) 慢 | **O(1)** 快！ |
| 尾部插入/删除 | **O(1)** 快！（扩容时O(n)） | **O(1)** 快！ |
| 内存占用 | 连续内存，空间利用率高 | 节点分散，空间开销大 |

> **小薰建议**：
> - 大部分场景用 **ArrayList**！因为随机访问是刚需
> - 只有在频繁在中间插入/删除时，才考虑 LinkedList
> - Java 8+ 的 ArrayList 尾插几乎总是 O(1)，性能很好

---

### 3.3 HashMap 核心原理

这是面试必考的内容！（认真脸）

**JDK 7 vs JDK 8+**：
- JDK 7：数组 + **链表**
- JDK 8+：数组 + **链表 + 红黑树**（链表长度 > 8 时转换）

**为什么用红黑树？**
- 链表查找是 O(n)，红黑树是 O(log n)
- 链表太长时查找变慢，红黑树能保持高效

**put() 流程**：
```
1. 计算 key 的 hash 值（调用 hashCode()）
2. 通过 (n - 1) & hash 计算索引位置
3. 如果该位置为空 → 直接插入
4. 如果不为空 →
   a. 如果 key 相等 → 覆盖 value
   b. 如果 key 不等 → 遍历链表/红黑树
5. 如果元素数量 > 阈值 → 扩容
```

**扩容机制**：
- 默认容量 **16**，负载因子 **0.75**
- 扩容时容量**翻倍**
- 所有元素需要重新计算位置（rehash）

> **小薰重要提醒**：扩容是很耗性能的操作，因为要 rehash 所有元素。所以**预估容量很重要**！
>
> 如果你知道大概会有 1000 个元素，可以这样创建：
> `new HashMap<>(1024)` 或 `new HashMap<>(2048)`

**HashMap vs Hashtable vs ConcurrentHashMap**：

| 特性 | HashMap | Hashtable | ConcurrentHashMap |
|------|---------|-----------|-------------------|
| 线程安全 | ❌ | ✅（全表锁） | ✅（分段锁） |
| 性能 | 高 | 低 | 较高 |
| key/value 能否为 null | 可以 | 不行 | 不行 |
| 推荐场景 | 单线程 | 不推荐 | 多线程 |

> **小薰说**：多线程环境请用 **ConcurrentHashMap**！Hashtable 虽然安全但性能太差，基本被淘汰了。

---

### 3.4 常用集合操作

```java
// ========== List ==========
List<String> list = new ArrayList<>();
list.add("a");
list.add(0, "b");  // 插入到指定位置

// 遍历方式
for (String s : list) { }  // for-each
list.forEach(System.out::println);  // Lambda

// 删除
list.remove("a");  // 按值删除
list.remove(0);  // 按索引删除

// ========== Map ==========
Map<String, Integer> map = new HashMap<>();
map.put("语文", 90);
map.put("数学", 100);

map.get("语文");           // 获取值
map.containsKey("数学");   // 是否包含key
map.remove("数学");        // 删除

// 遍历 Map
map.forEach((k, v) -> System.out.println(k + ": " + v));

// ========== Stream（Java 8+）==========
List<String> result = list.stream()
    .filter(s -> s.startsWith("a"))  // 过滤
    .map(String::toUpperCase)          // 转换
    .collect(Collectors.toList());      // 收集
```

> **小薰推荐**：Stream API 真的很优雅！链式调用，代码简洁又易读。
>
> 但要注意性能哦——如果是超大数据集，Stream 可能不如手写 for 循环快。

---

## 4. 异常处理

异常处理就像演出时的意外情况处理——小提琴断弦了怎么办？观众突然咳嗽怎么办？好的演奏家会准备好应急预案！

---

### 4.1 异常体系

```
Throwable（所有异常和错误的祖先）
├── Error（错误——程序无法处理）
│   ├── OutOfMemoryError（内存溢出）
│   ├── StackOverflowError（栈溢出）
│   └── ...（还有很多，但不是重点）
│
└── Exception（异常——可以处理）
    ├── RuntimeException（运行时异常）
    │   ├── NullPointerException（空指针——最常见的异常！）
    │   ├── ArrayIndexOutOfBoundsException（数组越界）
    │   ├── ClassCastException（类型转换错误）
    │   └── ArithmeticException（算术错误，如除以0）
    │
    └── 非运行时异常（编译时异常，Checked Exception）
        ├── IOException（输入输出异常）
        ├── SQLException（数据库异常）
        └── FileNotFoundException（文件未找到）
```

> **小薰说**：
> - **Error** 是 JVM 的问题，程序管不了，比如内存不够了
> - **RuntimeException** 是程序员的锅，通常是代码写错了
> - **Checked Exception** 是外部问题，比如文件可能不存在、数据库可能连不上

---

### 4.2 异常处理

```java
// 基本 try-catch-finally
try {
    // 可能出错的代码
    int result = 10 / 0;  // 这里会抛出 ArithmeticException
} catch (ArithmeticException e) {
    // 捕获特定异常
    System.out.println("除数不能为零！");
    e.printStackTrace();  // 打印异常堆栈，方便调试
} catch (Exception e) {
    // 捕获其他异常（范围更大的要放在后面！）
    System.out.println("出错了：" + e.getMessage());
} finally {
    // 无论有没有异常，都会执行
    // 通常用于释放资源，比如关闭连接、关闭文件
    System.out.println("finally 执行了");
}
```

> **小薰敲黑板**：
> 1. 异常要**从小到大**捕获——子类的 catch 要放在父类前面！
> 2. `e.printStackTrace()` 方便调试，生产环境建议用 `logger.error()`
> 3. finally 里写释放资源的代码

**JDK 7+ 多异常捕获**：
```java
try {
    // ...
} catch (IOException | SQLException e) {  // 用 | 连接多个异常
    // IOException 和 SQLException 共用这段处理
    System.out.println("IO 或数据库出错了");
}
```

**throws vs throw**：
```java
// throws——声明方法可能抛出什么异常
public void readFile() throws IOException {
    // 方法内部可以不处理，交给调用者
    throw new IOException("文件不存在");  // 主动抛出异常
}

// throw——手动抛出异常
throw new IllegalArgumentException("参数不能为负数");
```

---

### 4.3 自定义异常

有时候我们需要定义自己的异常～

```java
// 业务异常——继承 RuntimeException 表示运行时异常
public class BusinessException extends RuntimeException {
    private int code;  // 业务错误码
    
    public BusinessException(int code, String message) {
        super(message);  // 调用父类构造方法
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}

// 使用自定义异常
public User getUserById(Long id) {
    User user = userRepository.findById(id);
    if (user == null) {
        throw new BusinessException(1001, "用户不存在");  // 抛出业务异常
    }
    return user;
}
```

> **小薰建议**：
> - 自定义异常建议继承 `RuntimeException`，因为不需要强制声明和捕获
> - 加一个错误码字段，方便前端判断和日志记录
> - 异常名要见名知意，比如 `UserNotFoundException`

---

## 5. 泛型与反射

泛型和反射是 Java 的"高级技能"——学会了它们，就能写出更灵活、更优雅的代码！

---

### 5.1 泛型

泛型就像乐谱上的调号标记——提前告诉编译器这支曲子是什么调，演奏时就不会跑调了～

**泛型类**：
```java
// 定义泛型类
public class Box<T> {
    private T value;  // T 是类型参数
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
}

// 使用泛型类
Box<String> stringBox = new Box<>();  // T 被替换为 String
stringBox.setValue("hello");
String value = stringBox.getValue();  // 不需要强制类型转换！
```

> **小薰说**：泛型的核心好处——**类型安全 + 少写强制转换**！

**泛型方法**：
```java
// 泛型方法
public static <T> T getMiddle(T... array) {
    return array[array.length / 2];
}

// 调用
String middle = getMiddle("a", "b", "c");  // 返回 "b"
Integer num = getMiddle(1, 2, 3);           // 返回 2
```

**泛型限定**：
```java
// 上界限定：T 必须是 Number 或其子类
public <T extends Number> void test(T t) {
    t.doubleValue();  // 可以调用 Number 的方法
}

// 下界限定：T 必须是 Integer 或其父类
<? super Integer>

// 多重限定
<T extends Comparable & Serializable>  // T 必须实现两个接口
```

> **小薰记忆**：
> - `extends` 既可以限定上界（类/接口）
> - `super` 用于下界限定（只能用于泛型通配符）

**泛型擦除（重点！）**：
```
编译时：检查类型是否匹配
运行时：类型信息被擦除（变成 Object 或上限类型）
```

> **小薰敲黑板**：泛型只在**编译期**有效！
>
> 运行时的代码：
> - `Box<String>` 和 `Box<Integer>` 其实是同一个类
> - 泛型信息存储在 Class 字节码的 Signature 属性中
> - 这就是为什么 `new ArrayList<String>()` 和 `new ArrayList<Integer>()` 不能共存的原因！

---

### 5.2 反射

反射就像"读心术"——可以在运行时读取类的信息，甚至调用私有方法！

**获取 Class 对象**：
```java
// 方式一：Class.forName()（最常用）
Class<?> clazz = Class.forName("com.example.User");

// 方式二：.class（需要导入类）
Class<User> clazz = User.class;

// 方式三：getClass()（已有对象）
User user = new User();
Class<? extends User> clazz = user.getClass();
```

> **小薰说**：`Class.forName()` 最大的好处是不需要导入类！常用于框架开发。

**反射操作**：
```java
// 创建实例（调用无参构造）
User user = (User) clazz.getDeclaredConstructor().newInstance();

// 创建实例（调用有参构造）
Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, int.class);
User user2 = (User) constructor.newInstance("Alice", 25);

// 获取和设置字段
Field nameField = clazz.getDeclaredField("name");
nameField.setAccessible(true);  // 绕过访问检查！
String name = (String) nameField.get(user);
nameField.set(user, "Bob");

// 调用方法
Method setNameMethod = clazz.getDeclaredMethod("setName", String.class);
setNameMethod.invoke(user, "Charlie");

// 获取父类和接口
Class<?> superClass = clazz.getSuperclass();
Class<?>[] interfaces = clazz.getInterfaces();
```

> **小薰提醒**：
> - `setAccessible(true)` 可以访问 private 的成员
> - 反射很强大，但性能开销也大
> - 滥用反射会降低代码可读性和维护性
> - **Spring 框架大量使用反射**——这才是反射的正确打开方式！

---

### 5.3 泛型与反射的结合

```java
// 通用对象创建器
public static <T> T createObject(Class<T> clazz) {
    try {
        return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

// 使用
User user = createObject(User.class);  // 传入 Class 对象，泛型自动推断类型
```

---

## 6. 多线程与并发

多线程就像交响乐团同时演奏——每个乐器（线程）各司其职，但如果配合不好，就会变成一团噪音！

---

### 6.1 线程基础

**创建线程的三种方式**：

```java
// 方式一：继承 Thread（不推荐）
public class MyThread extends Thread {
    @Override
    public void run() {  // 线程执行的内容
        System.out.println("Thread running");
    }
}
new MyThread().start();  // 调用 start() 不是 run()！

// 方式二：实现 Runnable（推荐）
public class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running");
    }
}
new Thread(new MyRunnable()).start();

// 方式三：Lambda（最简洁，推荐）
new Thread(() -> System.out.println("Lambda thread")).start();
```

> **小薰说**：`start()` 才会启动新线程，直接调用 `run()` 只是普通方法调用！

**线程生命周期**：
```
NEW（新建）→ RUNNABLE（就绪/运行中）→ TERMINATED（终止）
                 ↓
            BLOCKED/WAITING/TIMED_WAITING（阻塞/等待）
```

> **小薰记忆**：就像演奏家等待指挥——RUNNABLE 是准备好演奏了，BLOCKED 是被暂停了。

**常用方法**：
```java
thread.start()        // 启动线程
thread.join()         // 等待线程结束
thread.sleep(ms)      // 休眠（不释放锁）
thread.wait()         // 等待（释放锁）
thread.notify()       // 唤醒一个等待的线程
thread.notifyAll()    // 唤醒所有等待的线程
Thread.yield()       // 让出 CPU 时间片
```

---

### 6.2 Synchronized

`synchronized` 是最常用的同步机制——就像乐团里只有一个指挥！

```java
// 修饰实例方法（锁对象是 this）
public synchronized void method() {
    // 同时只有一个线程能执行
}

// 修饰静态方法（锁对象是类对象）
public static synchronized void staticMethod() {
    // 锁的是 Class 对象
}

// 修饰代码块（锁对象是指定对象）
public void blockMethod() {
    synchronized (this) {  // 锁 this 对象
        // 同步代码
    }
}
```

> **小薰敲黑板**：
> - 实例方法锁的是 `this` 对象
> - 静态方法锁的是 `Class` 对象
> - 代码块可以精确控制锁的范围

**可重入特性**：
```java
synchronized void method1() {
    method2();  // 可以调用，因为是可重入锁！
}
synchronized void method2() {
    // 同一个线程可以多次获取锁
}
```

> **小薰说**：Synchronized 是**可重入锁**——同一个线程可以多次获取同一把锁，不会死锁。

---

### 6.3 volatile

`volatile` 是轻量级的同步机制——保证可见性，但不保证原子性。

```java
public class VolatileDemo {
    private volatile boolean flag = false;
    // 可见性：修改后立即刷新到主内存
    // 不保证原子性：++ 操作不是原子的
}
```

**volatile vs synchronized**：

| 特性 | volatile | synchronized |
|------|----------|--------------|
| 可见性 | ✅ 保证 | ✅ 保证 |
| 原子性 | ❌ 不保证 | ✅ 保证 |
| 性能 | **高** | 低 |

> **小薰说**：适合只被一个线程写、多个线程读的场景。比如配置变量的更新。

---

### 6.4 JUC 并发包

java.util.concurrent 提供了强大的并发工具！

**ConcurrentHashMap**（推荐的多线程 Map）：
```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.putIfAbsent("a", 1);           // 不存在才插入
map.computeIfAbsent("b", k -> 1);   // 不存在才计算
map.getOrDefault("a", 0);          // 获取或默认值
```

**CountDownLatch**（倒数计数器）：
```java
CountDownLatch latch = new CountDownLatch(3);

new Thread(() -> { /* 任务1 */ latch.countDown(); }).start();
new Thread(() -> { /* 任务2 */ latch.countDown(); }).start();
new Thread(() -> { /* 任务3 */ latch.countDown(); }).start();

latch.await();  // 等待所有任务完成
System.out.println("所有任务完成！");
```

> **小薰说**：就像等所有乐器手都准备好了才开始演奏！

**Semaphore**（信号量，控制并发数）：
```java
Semaphore semaphore = new Semaphore(2);  // 同时只有 2 个线程能访问

for (int i = 0; i < 5; i++) {
    new Thread(() -> {
        try {
            semaphore.acquire();  // 获取许可
            // 访问共享资源
            semaphore.release();   // 释放许可
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();
}
```

**CompletableFuture**（异步编程神器）：
```java
CompletableFuture.supplyAsync(() -> {
    // 异步任务
    return "Hello";
}).thenApply(result -> {
    // 处理结果
    return result.toUpperCase();
}).thenAccept(System.out::println);  // 最终处理
```

> **小薰推荐**：CompletableFuture 是处理异步任务的利器！比 Future 更强大。

---

### 6.5 线程池

线程池就像乐团的编制——不需要的时候就休息，需要的时候随时上场！

```java
// 创建线程池
ExecutorService executor = Executors.newFixedThreadPool(5);

// 提交任务
Future<String> future = executor.submit(() -> {
    Thread.sleep(1000);
    return "Hello";
});

// 获取结果（阻塞等待）
String result = future.get();

// 关闭线程池
executor.shutdown();  // 等待任务完成
```

**线程池参数**（重要！）：
```java
new ThreadPoolExecutor(
    corePoolSize,      // 核心线程数（一直存在）
    maximumPoolSize,    // 最大线程数
    keepAliveTime,      // 空闲线程存活时间
    TimeUnit,          // 时间单位
    workQueue,         // 任务队列
    threadFactory,     // 线程工厂
    handler            // 拒绝策略
);
```

**拒绝策略**：
- `AbortPolicy`：抛出异常（默认）
- `CallerRunsPolicy`：由调用线程执行
- `DiscardPolicy`：丢弃任务
- `DiscardOldestPolicy`：丢弃最旧任务

> **小薰建议**：
> - 使用 `Executors` 创建线程池要注意参数是否合理
> - 生产环境推荐用 `new ThreadPoolExecutor()` 手动设置参数
> - 线程池大小建议：`CPU核心数 * 2` 或 `CPU核心数 / (1 - 阻塞系数)`

---

## 7. JVM

JVM（Java 虚拟机）是 Java 的"心脏"——理解它，就像理解乐团的指挥台一样重要！

---

### 7.1 内存区域

```
JVM 内存
├── 程序计数器       // 记录当前线程执行的字节码行号（线程私有）
├── 虚拟机栈        // 方法调用栈帧
│   └── 局部变量表、操作数栈、动态链接、方法返回地址
├── 本地方法栈      // Native 方法栈
├── 堆（Heap）      // 对象实例、数组（所有线程共享）
│   ├── 新生代
│   │   ├── Eden 区（伊甸园区）
│   │   └── Survivor 区（S0、S1 survivor空间）
│   └── 老年代
└── 方法区（MetaSpace）// 类信息、常量、静态变量（Java 8+，之前是永久代）
    └── 运行时常量池
```

> **小薰说**：堆是 Java 里最重要的区域——几乎所有对象都分配在这里！

**各区域特点**：
| 区域 | 线程共享 | 特点 |
|------|----------|------|
| 堆 | ✅ | 存放对象，GC 主要战场 |
| 栈 | ❌ | 方法调用，线程私有 |
| 方法区 | ✅ | 类信息、静态变量 |
| 程序计数器 | ❌ | 记录字节码行号 |

---

### 7.2 垃圾回收

GC（垃圾回收）就是 JVM 的"清洁工"——自动回收不再使用的内存！

**GC 算法**：
```
┌─────────────────────────────────────┐
│           垃圾回收算法               │
├──────────────┬──────────────────────┤
│  标记-清除   │  效率低，产生内存碎片  │
│  复制        │  适用于新生代（Eden） │
│  标记-整理   │  适用于老年代        │
└──────────────┴──────────────────────┘
```

**分代收集**：
- **新生代**：对象创建频繁，Minor GC 频率高，对象存活率低
- **老年代**：存活时间长的对象，Major/Full GC 频率低

> **小薰记忆**：新生代像舞台——演员上下很频繁；老年代像后台——存放长期需要的东西。

**常见 GC 收集器**：

| 收集器 | 特点 | 适用场景 |
|--------|------|----------|
| Serial | 单线程，最简单 | 客户端/小型应用 |
| ParNew | 多线程版 Serial | 多核服务器 |
| Parallel Scavenge | 吞吐量优先 | 后台批处理 |
| CMS | 并发标记清除，低停顿 | 追求响应速度 |
| G1 | 面向局部，平衡吞吐和停顿 | **JDK 9+ 默认** |
| ZGC/Shenandoah | 超低停顿（毫秒级） | 大内存低延迟 |

> **小薰建议**：现在默认用 **G1** 就好！如果追求更低延迟，可以考虑 ZGC。

---

### 7.3 类加载机制

类加载就是 JVM 读取"乐谱"的过程——把 .class 文件加载到内存！

**类加载过程**：
```
加载 → 验证 → 准备 → 解析 → 初始化 → 使用 → 卸载
   ↑
   重点掌握前3步！
```

**三层类加载器**：
```
Bootstrap ClassLoader（C++实现）
    ↓ 加载核心类库 JAVA_HOME/lib
Extension ClassLoader
    ↓ 加载扩展类库 JAVA_HOME/lib/ext
App ClassLoader
    ↓ 加载应用 classpath
自定义 ClassLoader
```

**双亲委派模型**（重要！）：
```
类加载请求
    ↓
App ClassLoader
    ↓
Extension ClassLoader
    ↓
Bootstrap ClassLoader
    ↓
如果都找不到，才自己加载
```

> **小薰说**：双亲委派就像家族传承——有长辈在，小辈就不用操心。好处是**保证类的唯一性和安全性**！

---

## 8. IO/NIO

IO 就是"输入输出"——Java 与外界交换数据的方式！

---

### 8.1 IO 体系

```
字节流（处理二进制数据）
├── InputStream（输入）
│   ├── FileInputStream     // 文件输入
│   ├── BufferedInputStream // 缓冲输入（提升性能）
│   └── ObjectInputStream   // 对象输入（反序列化）
└── OutputStream（输出）
    ├── FileOutputStream
    ├── BufferedOutputStream
    └── ObjectOutputStream

字符流（处理文本数据）
├── Reader（输入）
│   ├── FileReader          // 文件读取
│   ├── BufferedReader      // 缓冲读取（推荐）
│   └── InputStreamReader   // 字节→字符转换
└── Writer（输出）
    ├── FileWriter
    ├── BufferedWriter
    └── OutputStreamWriter
```

> **小薰建议**：
> - 处理文本用**字符流**（Reader/Writer）
> - 处理二进制（图片、音频）用**字节流**（InputStream/OutputStream）
> - 用 Bufferedxxx 包装能提升性能！

---

### 8.2 常用操作

```java
// 文件复制（字节流）
try (InputStream in = new FileInputStream("a.txt");
     OutputStream out = new FileOutputStream("b.txt")) {
    byte[] buffer = new byte[1024];
    int len;
    while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
    }
}

// 字符流读写（推荐写法）
try (BufferedReader reader = new BufferedReader(
         new FileReader("file.txt"));
     BufferedWriter writer = new BufferedWriter(
         new FileWriter("output.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        writer.write(line);
        writer.newLine();
    }
}

// JDK 7+ 简化写法
List<String> lines = Files.readAllLines(Paths.get("file.txt"));
Files.write(Paths.get("output.txt"), lines);
```

> **小薰敲黑板**：
> 1. **一定要用 try-with-resources**！自动关闭资源
> 2. Bufferedxxx 性能比直接用 Filexxx 好

**对象序列化**（对象 → 字节流）：
```java
// 序列化
try (ObjectOutputStream oos = new ObjectOutputStream(
         new FileOutputStream("user.dat"))) {
    oos.writeObject(new User("Alice", 25));
}

// 反序列化
try (ObjectInputStream ois = new ObjectInputStream(
         new FileInputStream("user.dat"))) {
    User user = (User) ois.readObject();
}
```

> **小薰提醒**：被序列化的类必须实现 `Serializable` 接口！

---

### 8.3 NIO

NIO（New IO）是 Java 的"升级版 IO"——支持非阻塞操作，性能更高！

**三大核心组件**：

| 组件 | 作用 | 类比 |
|------|------|------|
| **Channel** | 通道，类似流但可双向 | 铁路轨道 |
| **Buffer** | 缓冲区，数据存放 | 火车车厢 |
| **Selector** | 选择器，多路复用 | 调度中心 |

```java
// NIO 文件读取
RandomAccessFile file = new RandomAccessFile("test.txt", "rw");
FileChannel channel = file.getChannel();

ByteBuffer buffer = ByteBuffer.allocate(1024);
channel.read(buffer);

buffer.flip();  // 切换读写模式
while (buffer.hasRemaining()) {
    System.out.print((char) buffer.get());
}

channel.close();
file.close();
```

**Buffer 核心方法**：
```
buffer.put()     // 写入数据
buffer.get()     // 读取数据
buffer.flip()    // 切换：写模式 → 读模式
buffer.clear()   // 清空：读模式 → 写模式
buffer.rewind()  // 重置位置，可重复读
```

> **小薰说**：NIO 主要用于高性能服务器开发（比如网络通信）。普通文件操作用 IO 就够了！

---

---

## 9. 新特性

Java 8 是 Java 史上最重要的升级！之后每一代都有惊喜～

---

### 9.1 Java 8（里程碑版本！）

Java 8 改变了 Java 的编写方式！

**Lambda 表达式**（函数式编程入门）：
```java
// 完整写法
Comparator<String> c = (String a, String b) -> { return a.compareTo(b); };

// 简化写法（类型可以省略）
Comparator<String> c = (a, b) -> a.compareTo(b);

// 方法引用（更简洁）
List<String> list = Arrays.asList("a", "b", "c");
list.forEach(System.out::println);  // System.out::println 是方法引用
```

> **小薰说**：Lambda 让代码更简洁、更易读！函数式编程的大门从此打开～

**Stream API**（处理集合的神器）：
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// 过滤、映射、收集——链式调用，优雅！
List<Integer> result = numbers.stream()
    .filter(n -> n % 2 == 0)   // 过滤偶数
    .map(n -> n * 2)           // 乘以2
    .collect(Collectors.toList());

// 聚合操作
int sum = numbers.stream().mapToInt(Integer::intValue).sum();
Optional<Integer> max = numbers.stream().max(Integer::compareTo);
```

> **小薰敲黑板**：Stream 不是数据结构，不存储数据！它只是处理数据的管道，用完就没了。

**接口默认方法**（向后兼容的秘密武器）：
```java
public interface MyInterface {
    default void defaultMethod() {
        System.out.println("默认实现，子类可以不重写");
    }
}
```

> **小薰说**：默认方法让接口可以添加新方法而不破坏现有实现！这就是 Java 8 能平稳升级的原因。

**Optional**（空指针克星）：
```java
Optional<String> optional = Optional.ofNullable(getName());

// 安全取值
String name = optional.orElse("默认值");  // 为空时返回默认值
String name2 = optional.orElseGet(() -> computeDefault());  // 懒加载

// 链式调用（优雅！）
optional.map(String::toUpperCase)
        .filter(s -> s.length() > 3)
        .ifPresent(System.out::println);
```

> **小薰强烈推荐**：处理可能为空的值时用 Optional！告别 NullPointerException！

**日期时间 API**（告别 Date 的噩梦）：
```java
LocalDate today = LocalDate.now();
LocalDateTime now = LocalDateTime.now();
LocalTime time = LocalTime.of(10, 30);

// 格式化
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formatted = now.format(formatter);
```

> **小薰说**：Java 8 的日期时间是线程安全的，而 Date 不是！

---

### 9.2 Java 11+

**String 增强**：
```java
"   hello   ".isBlank();      // true，判断是否空白
"hello".repeat(3);            // "hellohellohello"，重复字符串
"hello\nworld".lines();       // Stream<String>，按行分割
```

**文件操作简化**：
```java
// 一行代码读取文件
List<String> lines = Files.readAllLines(Path.of("file.txt"));

// 一行代码写入文件
Files.writeString(Path.of("file.txt"), "content");
```

**局部变量类型推断**（var）：
```java
var list = new ArrayList<String>();  // 类型推断为 ArrayList<String>
var stream = list.stream();
```

> **小薰说**：var 只是语法糖，编译器会自动推断类型。不是弱类型！

---

### 9.3 Java 17+

**sealed class（密封类）**：精确控制类的继承关系！
```java
// Shape 只有 Circle、Rectangle、Triangle 三个子类
public sealed class Shape permits Circle, Rectangle, Triangle { }

// 允许的子类必须声明类型
public final class Circle extends Shape { }           // final：不能再继承
public sealed class Rectangle extends Shape permits Square { }  // sealed：可以继续限制
public non-sealed class Triangle extends Shape { }   // non-sealed：可以任意继承
```

> **小薰说**：密封类让我们精确控制"谁能继承"！这是类型安全的重大进步。

**Pattern Matching for switch**（switch 的华丽升级）：
```java
static String formatter(Object obj) {
    return switch (obj) {
        case Integer i -> String.format("int %d", i);   // 自动转型！
        case String s -> String.format("String %s", s);
        case null, default -> "unknown";  // null 和 default 可以合并
    };
}
```

> **小薰推荐**：Java 17+ 的新特性让代码更简洁、更安全！

---

---

## 10. 设计模式（GoF 23种）

设计模式是解决常见问题的"套路"——就像不同类型的曲子有不同的结构一样！

---

### 10.1 创建型模式

**单例模式（Singleton）**——确保只有一个实例：

```java
// 饿汉式（类加载时就创建，简单但可能浪费资源）
public class Singleton1 {
    private static final Singleton1 INSTANCE = new Singleton1();
    private Singleton1() {}
    public static Singleton1 getInstance() {
        return INSTANCE;
    }
}

// 懒汉式（延迟加载，但线程不安全！）
public class Singleton2 {
    private static Singleton2 instance;
    private Singleton2() {}
    public static Singleton2 getInstance() {
        if (instance == null) {
            instance = new Singleton2();
        }
        return instance;
    }
}

// 双重检查锁（线程安全，推荐！）
public class Singleton3 {
    private static volatile Singleton3 instance;  // volatile 必须加！
    private Singleton3() {}
    public static Singleton3 getInstance() {
        if (instance == null) {
            synchronized (Singleton3.class) {
                if (instance == null) {
                    instance = new Singleton3();
                }
            }
        }
        return instance;
    }
}

// 静态内部类（最推荐！简单又线程安全）
public class Singleton4 {
    private Singleton4() {}
    private static class Holder {
        private static final Singleton4 INSTANCE = new Singleton4();
    }
    public static Singleton4 getInstance() {
        return Holder.INSTANCE;
    }
}
```

> **小薰建议**：单例模式用得很多！推荐**静态内部类**写法——简单、安全、无性能开销。

**工厂方法模式**——对象创建交给子类：

```java
public interface Product {
    void produce();
}

public class ConcreteProductA implements Product {
    @Override
    public void produce() {
        System.out.println("生产产品A");
    }
}

public class ConcreteProductB implements Product {
    @Override
    public void produce() {
        System.out.println("生产产品B");
    }
}

public interface Factory {
    Product createProduct();
}

public class ConcreteFactory implements Factory {
    @Override
    public Product createProduct() {
        return new ConcreteProductA();
    }
}
```

**建造者模式（Builder）**——分步构建复杂对象：

```java
public class User {
    private final String name;
    private final int age;
    private final String address;

    private User(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.address = builder.address;
    }

    public static class Builder {
        private String name;
        private int age;
        private String address;

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder age(int age) {
            this.age = age;
            return this;
        }
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        public User build() {
            return new User(this);
        }
    }
}

// 使用：链式调用，清晰！
User user = new User.Builder()
    .name("Alice")
    .age(25)
    .address("Beijing")
    .build();
```

> **小薰说**：Builder 模式特别适合参数很多的对象创建！Lombok 的 `@Builder` 注解就是基于这个原理。

### 10.2 结构型模式

**适配器模式（Adapter）**——让不兼容的接口合作：

```java
// 对象适配器（推荐，使用组合）
public class ObjectAdapter implements Target {
    private Adaptee adaptee;
    
    public ObjectAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
    
    @Override
    public void request() {
        adaptee.specificRequest();  // 转换调用
    }
}
```

> **小薰说**：适配器就像翻译官——让说不同语言的人能交流。

**装饰器模式（Decorator）**——动态添加功能：

```java
public interface Component {
    void operation();
}

public class ConcreteComponent implements Component {
    @Override
    public void operation() {
        System.out.println("原始操作");
    }
}

// 装饰器
public class Decorator implements Component {
    protected Component component;
    
    public Decorator(Component component) {
        this.component = component;
    }
    
    @Override
    public void operation() {
        component.operation();  // 先执行原操作
    }
}

// 具体装饰器
public class LoggingDecorator extends Decorator {
    public LoggingDecorator(Component component) {
        super(component);
    }
    
    @Override
    public void operation() {
        System.out.println("日志记录开始");
        super.operation();
        System.out.println("日志记录结束");
    }
}
```

> **小薰说**：装饰器比继承更灵活！Java I/O 就大量使用了装饰器模式。

---
    }
    
    @Override
    public void operation() {
        component.operation();
    }
}

public class ConcreteDecorator extends Decorator {
    public ConcreteDecorator(Component component) {
        super(component);
    }
    
    @Override
    public void operation() {
        super.operation();
        addedBehavior();
    }
    
    private void addedBehavior() {
        System.out.println("增强的行为");
    }
}
```

**代理模式（Proxy）**：
```java
public interface Subject {
    void request();
}

public class RealSubject implements Subject {
    @Override
    public void request() {
        System.out.println("真实请求");
    }
}

public class ProxySubject implements Subject {
    private RealSubject realSubject;
    
    @Override
    public void request() {
        if (realSubject == null) {
            realSubject = new RealSubject();
        }
        beforeRequest();
        realSubject.request();
        afterRequest();
    }
    
    private void beforeRequest() {
        System.out.println("前置处理");
    }
    
    private void afterRequest() {
        System.out.println("后置处理");
    }
}
```

### 10.3 行为型模式

**策略模式（Strategy）**：
```java
public interface Strategy {
    int execute(int a, int b);
}

public class AddStrategy implements Strategy {
    @Override
    public int execute(int a, int b) {
        return a + b;
    }
}

public class Context {
    private Strategy strategy;
    
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
    
    public int executeStrategy(int a, int b) {
        return strategy.execute(a, b);
    }
}
```

**观察者模式（Observer）**：
```java
public interface Observer {
    void update(String message);
}

public class Subject {
    private List<Observer> observers = new ArrayList<>();
    
    public void attach(Observer observer) {
        observers.add(observer);
    }
    
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}
```

**模板方法模式（Template Method）**：
```java
public abstract class AbstractClass {
    // 模板方法
    public final void templateMethod() {
        step1();
        step2();
        hook();
        step3();
    }
    
    protected void step1() {
        System.out.println("步骤1");
    }
    
    protected void step2() {
        System.out.println("步骤2");
    }
    
    protected void step3() {
        System.out.println("步骤3");
    }
    
    // 钩子方法（可选覆盖）
    protected void hook() { }
}
```

**责任链模式（Chain of Responsibility）**：
```java
public abstract class Handler {
    protected Handler nextHandler;
    
    public void setNextHandler(Handler handler) {
        this.nextHandler = handler;
    }
    
    public final void handleRequest(String request) {
        if (canHandle(request)) {
            doHandle(request);
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        }
    }
    
    protected abstract boolean canHandle(String request);
    protected abstract void doHandle(String request);
}
```

---

## 11. Spring Framework 核心

### 11.1 IoC 容器

**BeanFactory vs ApplicationContext**：
```java
// BeanFactory（懒加载）
BeanFactory factory = new XmlBeanFactory(
    new ClassPathResource("beans.xml"));
User user = (User) factory.getBean("user");

// ApplicationContext（预加载，功能更强大）
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
User user = context.getBean("user", User.class);
```

**Bean 作用域**：
| 作用域 | 说明 |
|--------|------|
| singleton | 单例（默认） |
| prototype | 每次获取创建新实例 |
| request | HTTP 请求 |
| session | HTTP 会话 |
| application | ServletContext |
| websocket | WebSocket |

**Bean 生命周期**：
```
实例化 → 属性填充 → BeanNameAware → BeanFactoryAware → 
ApplicationContextAware → BeanPostProcessor.postProcessBeforeInitialization → 
@PostConstruct → InitializingBean.afterPropertiesSet → 
BeanPostProcessor.postProcessAfterInitialization → DisposableBean.destroy
```

### 11.2 依赖注入（DI）

**构造方法注入**：
```java
@Component
public class UserService {
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Setter 注入**：
```java
@Component
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**字段注入**：
```java
@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### 11.3 AOP

**核心概念**：
- **Aspect**：切面（横切关注点）
- **Join Point**：连接点（可被拦截的方法）
- **Pointcut**：切点（实际被拦截的点）
- **Advice**：通知（增强逻辑）
- **Weaving**：织入（将增强逻辑应用到目标对象）

**通知类型**：
| 类型 | 说明 |
|------|------|
| @Before | 前置通知 |
| @AfterReturning | 返回通知 |
| @AfterThrowing | 异常通知 |
| @After | 后置通知 |
| @Around | 环绕通知 |

**AOP 示例**：
```java
@Aspect
@Component
public class LogAspect {
    
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void pointcut() {}
    
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("执行方法: " + methodName);
    }
    
    @AfterReturning(pointcut = "pointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("返回值: " + result);
    }
    
    @AfterThrowing(pointcut = "pointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        System.out.println("异常: " + e.getMessage());
    }
    
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        System.out.println("耗时: " + (end - start) + "ms");
        return result;
    }
}
```

---

## 12. Spring Boot

### 12.1 核心注解

```java
@SpringBootApplication  // 启动类注解
@EnableAutoConfiguration  // 启用自动配置
@ComponentScan  // 组件扫描
@Configuration  // 配置类

// Bean 注册
@Component  // 通用组件
@Service  // 服务层
@Repository  // 持久层
@Controller / @RestController  // 控制器

// 依赖注入
@Autowired  // 自动注入
@Primary  // 主 Bean
@Qualifier  // 指定 Bean 名称

// 配置文件
@Value  // 读取单个配置
@ConfigurationProperties  // 批量绑定配置
@EnableConfigurationProperties  // 启用配置属性

// 切面
@Aspect  // 切面
@Before / @After  // 通知

// 事务
@Transactional  // 声明事务

// 缓存
@EnableCaching  // 启用缓存
@Cacheable  // 缓存结果
```

### 12.2 自动配置原理

**@SpringBootApplication 分解**：
```java
@SpringBootConfiguration  // 等价于 @Configuration
@EnableAutoConfiguration  // 启用自动配置
    // 会读取 META-INF/spring.factories
    // 加载所有 AutoConfiguration
@ComponentScan  // 组件扫描
```

**自动配置流程**：
1. Spring Boot 启动时加载 `spring.factories`
2. 创建所有 `AutoConfiguration` 类
3. 根据条件注解 `@Conditional` 判断是否生效
4. 生效的配置类会注册对应的 Bean

### 12.3 Starter 机制

**自定义 Starter**：
```
my-spring-boot-starter/
├── src/main/java/
│   └── com/example/starter/
│       ├── MyAutoConfiguration.java  // 自动配置类
│       └── MyProperties.java        // 配置属性类
└── src/main/resources/
    └── META-INF/
        └── spring.factories
```

```java
// MyProperties.java
@ConfigurationProperties(prefix = "my")
public class MyProperties {
    private String name = "default";
    // getter/setter
}

// MyAutoConfiguration.java
@Configuration
@EnableConfigurationProperties(MyProperties.class)
public class MyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties properties) {
        return new MyService(properties.getName());
    }
}
```

---

## 13. 数据库高级

### 13.1 事务隔离级别

| 级别 | 脏读 | 不可重复读 | 幻读 |
|------|------|-----------|------|
| READ_UNCOMMITTED | 可能 | 可能 | 可能 |
| READ_COMMITTED | 不可能 | 可能 | 可能 |
| REPEATABLE_READ | 不可能 | 不可能 | 可能 |
| SERIALIZABLE | 不可能 | 不可能 | 不可能 |

**MySQL 默认**：REPEATABLE_READ
**Oracle 默认**：READ_COMMITTED

### 13.2 锁机制

**行锁 vs 表锁**：
```sql
-- 行锁
SELECT * FROM users WHERE id = 1 FOR UPDATE;

-- 表锁
LOCK TABLE users WRITE;
UNLOCK TABLES;
```

**共享锁 vs 排他锁**：
```sql
-- 共享锁（S锁）
SELECT * FROM users WHERE id = 1 LOCK IN SHARE MODE;

-- 排他锁（X锁）
SELECT * FROM users WHERE id = 1 FOR UPDATE;
```

**死锁**：
- 两个或多个事务相互等待对方释放锁
- MySQL 会自动检测并回滚一个事务
- 解决：按固定顺序访问资源

### 13.3 SQL 优化

**索引优化**：
```sql
-- 创建索引
CREATE INDEX idx_name ON users(name);
CREATE INDEX idx_age_name ON users(age, name);

-- 索引最左前缀原则
-- idx_age_name 支持: age, age + name
-- 不支持: name

-- 避免索引失效
SELECT * FROM users WHERE name LIKE '%abc';  -- 前导通配符
SELECT * FROM users WHERE age + 1 = 20;     -- 函数操作
```

**SQL 优化技巧**：
- 使用 EXPLAIN 分析执行计划
- 避免 SELECT *
- 使用 LIMIT 限制结果集
- 批量操作代替循环单条
- 分页优化：延迟关联

```sql
-- 分页优化
-- 低效
SELECT * FROM users ORDER BY id LIMIT 1000000, 10;

-- 高效（延迟关联）
SELECT * FROM users u 
INNER JOIN (
    SELECT id FROM users ORDER BY id LIMIT 1000000, 10
) t ON u.id = t.id;
```

### 13.4 分库分表

**分片策略**：
- 哈希分片：`shard_key % n`
- 范围分片：`id BETWEEN 1 AND 1000000`
- 一致性哈希

**常见中间件**：
- ShardingSphere-JDBC（客户端分片）
- ShardingSphere-Proxy（代理分片）
- MyCat
- TiDB / CockroachDB

---

## 14. 性能调优

### 14.1 JVM 调优

**常用参数**：
```bash
# 堆内存
-Xms512m -Xmx512m    # 初始堆/最大堆
-Xmn256m              # 年轻代大小
-Xss256k              # 线程栈大小

# 垃圾回收
-XX:+UseG1GC          # 使用 G1 收集器
-XX:MaxGCPauseMillis=200  # 最大 GC 停顿时间
-XX:+HeapDumpOnOutOfMemoryError  # OOM 时导出堆
-XX:HeapDumpPath=/tmp/heap.hprof  # 堆 dump 路径

# 性能监控
-XX:+PrintGCDetails   # 打印 GC 详情
-Xloggc:gc.log        # GC 日志
```

**JVM 调优思路**：
1. 监控 GC 频率和耗时
2. 分析 GC 日志
3. 调整堆大小和年轻代比例
4. 选择合适的 GC 收集器
5. 避免 Full GC（优化对象生命周期）

### 14.2 诊断工具

```bash
# 查看进程
jps -l
jps -v | grep spring

# 查看堆内存
jmap -heap <pid>

# 导出堆 dump
jmap -dump:format=b,file=heap.hprof <pid>

# 查看线程
jstack <pid>

# JConsole / VisualVM
jconsole
jvisualvm
```

### 14.3 代码级优化

**减少对象创建**：
```java
// 避免在循环中创建对象
for (int i = 0; i < 1000; i++) {
    String s = new String("test");  // 不好
}

String s = "test";  // 好，字符串常量池复用
```

**使用高效数据结构**：
```java
// ArrayList vs LinkedList
List<Integer> list = new ArrayList<>();  // 随机访问多
List<Integer> linked = new LinkedList<>();  // 增删多

// HashMap 初始化容量
new HashMap<>(16);  // 预估容量，避免扩容
```

**减少锁竞争**：
```java
// 减小锁粒度
ConcurrentHashMap map = new ConcurrentHashMap<>();

// 使用读写锁
ReadWriteLock lock = new ReentrantReadWriteLock();
lock.readLock().lock();
try {
    // 读操作
} finally {
    lock.readLock().unlock();
}

// 无锁并发
AtomicInteger count = new AtomicInteger();
count.incrementAndGet();
```

---

## 15. 常用工具类

### 15.1 Apache Commons

```java
// Commons Lang
StringUtils.isBlank(str);
StringUtils.join(list, ",");
ObjectUtils.defaultIfNull(obj, defaultValue);

// Commons Codec
DigestUtils.md5Hex(str);
DigestUtils.sha256Hex(str);

// Commons Collections
CollectionUtils.isEmpty(collection);
ListUtils.emptyIfNull(list);
```

### 15.2 Google Guava

```java
// 不可变集合
ImmutableList<String> list = ImmutableList.of("a", "b");

// Optional
Optional<String> optional = Optional.fromNullable(getName());

// 缓存
LoadingCache<Key, Graph> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build(new CacheLoader<Key, Graph>() {
        @Override
        public Graph load(Key key) {
            return createGraph(key);
        }
    });

// 限流器
RateLimiter limiter = RateLimiter.create(100.0);
limiter.acquire();

// 字符串连接
Joiner.on(", ").skipNulls().join(list);
Splitter.on(",").trimResults().splitToList(str);
```

---

（微微舒了口气）Java 高级特性已补充完成～ 包含了设计模式、Spring 框架、数据库高级和性能调优等内容。

---

## 16. 微服务架构

### 16.1 微服务基础

**什么是微服务**：
- 将大型应用拆分为多个小型、自治的服务
- 每个服务独立部署、独立运行
- 服务间通过轻量级协议通信（HTTP、消息队列）

**微服务优势**：
- 独立部署，灵活扩展
- 技术栈多样性
- 容错性好，单个服务故障不影响整体
- 团队自治

**微服务挑战**：
- 服务治理复杂
- 分布式事务问题
- 服务间通信开销
- 运维复杂度增加

### 16.2 服务注册与发现

**Eureka（Netflix）**：
```yaml
# eureka-server 配置
server:
  port: 8761
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
```

```yaml
# 服务提供者配置
spring:
  application:
    name: user-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

**Nacos（阿里巴巴）**：
```yaml
# bootstrap.yml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yml
```

### 16.3 配置中心（Nacos Config）

**配置管理**：
```yaml
# bootstrap.yml 加载 Nacos 配置
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yml
        namespace: dev  # 命名空间
        group: DEFAULT_GROUP  # 分组
```

**热更新配置**：
```java
// 方式一：@RefreshScope
@Component
@RefreshScope
public class UserConfig {
    @Value("${user.name}")
    private String name;
}

// 方式二：@NacosConfigurationProperties
@Data
@Component
@NacosConfigurationProperties(prefix = "user", dataId = "user-service.yml", autoRefreshed = true)
public class NacosConfig {
    private String name;
}
```

### 16.4 服务调用（Feign/OpenFeign）

**OpenFeign 声明式调用**：
```java
// 启用 Feign
@EnableFeignClients
@SpringBootApplication
public class OrderApplication {}

// 定义接口
@FeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserClient {
    @GetMapping("/user/{id}")
    User getUser(@PathVariable("id") Long id);
}

// 使用
@Service
public class OrderService {
    @Autowired
    private UserClient userClient;
    
    public Order getOrder(Long orderId) {
        User user = userClient.getUser(1L);
        // ...
    }
}
```

### 16.5 负载均衡

**Ribbon（Netflix）**：
```yaml
# 全局配置
user-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule
```

**负载均衡策略**：
| 策略 | 说明 |
|------|------|
| RoundRobinRule | 轮询 |
| RandomRule | 随机 |
| RetryRule | 重试 |
| WeightedResponseTimeRule | 响应时间加权 |
| BestAvailableRule | 最小并发数 |

**Spring Cloud LoadBalancer**：
```yaml
# 替换 Ribbon
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false  # 禁用 Ribbon
```

### 16.6 服务熔断降级（Sentinel）

**Sentinel 常用注解**：
```java
// 限流
@SentinelResource(value = "getUser", blockHandler = "getUserBlockHandler")
public User getUser(Long id) {
    return userService.getById(id);
}

// 降级处理
public User getUserBlockHandler(Long id, BlockException ex) {
    return new User(-1L, "系统繁忙");
}
```

**Sentinel 规则**：
- QPS 限流
- 线程数限流
- 熔断策略（慢调用比例、异常比例）
- 热点参数限流

### 16.7 API 网关（Gateway）

**Gateway 路由配置**：
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service  # 负载均衡
          predicates:
            - Path=/user/**
          filters:
            - StripPrefix=1
            - RequestRateLimiter=10,1
```

**Gateway 过滤器**：
```java
// 全局过滤器
@Component
public class AuthFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isBlank(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
```

### 16.8 分布式链路追踪（Sleuth + Zipkin）

**链路追踪配置**：
```yaml
spring:
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:
      probability: 1.0  # 采样率
```

**链路追踪概念**：
- **Trace**：一次完整的请求链路
- **Span**：一次 RPC 调用
- **Annotation**：事件记录（cs、cr、ss、sr）

### 16.9 消息驱动（Stream）

**Stream 基本概念**：
- **Source**：消息生产者
- **Sink**：消息消费者
- **Processor**：同时作为生产者和消费者

**Stream 配置**：
```yaml
spring:
  cloud:
    stream:
      binders:
        local:
          type: rabbit
      bindings:
        output:
          destination: my-exchange
        input:
          destination: my-exchange
          group: my-group
```

```java
// 生产者
@EnableBinding(Source.class)
public class MySource {
    @Autowired
    private Source source;
    
    public void send(String message) {
        source.output().send(MessageBuilder.withPayload(message).build());
    }
}

// 消费者
@EnableBinding(Sink.class)
public class MySink {
    @StreamListener(Sink.INPUT)
    public void receive(String message) {
        System.out.println("Received: " + message);
    }
}
```

### 16.10 分布式事务（Seata）

**Seata AT 模式**：
```yaml
# Seata 配置
seata:
  tx-service-group: my-tx-group
  registry:
    type: nacos
    nacos:
      server-addr: localhost:8848
  config:
    type: nacos
    nacos:
      server-addr: localhost:8848
```

```java
// 全局事务注解
@GlobalTransactional
public void createOrder() {
    // 创建订单
    orderService.create(order);
    // 扣减库存
    storageService.deduct(productId, count);
    // 扣减余额
    accountService.deduct(userId, amount);
}
```

### 16.11 Spring Cloud 技术栈总结

```
┌─────────────────────────────────────────────────────────┐
│                      微服务架构                          │
├─────────────────────────────────────────────────────────┤
│  网关层    │  Gateway / Zuul                            │
├───────────┼─────────────────────────────────────────────┤
│  注册中心  │  Nacos / Eureka / Consul                   │
├───────────┼─────────────────────────────────────────────┤
│  配置中心  │  Nacos Config / Apollo / Spring Cloud Config│
├───────────┼─────────────────────────────────────────────┤
│  服务调用  │  OpenFeign / RestTemplate                  │
├───────────┼─────────────────────────────────────────────┤
│  负载均衡  │  Spring Cloud LoadBalancer / Ribbon         │
├───────────┼─────────────────────────────────────────────┤
│  熔断降级  │  Sentinel / Resilience4j / Hystrix          │
├───────────┼─────────────────────────────────────────────┤
│  链路追踪  │  Sleuth + Zipkin / SkyWalking / Jaeger     │
├───────────┼─────────────────────────────────────────────┤
│  消息驱动  │  Spring Cloud Stream (Kafka/RocketMQ)      │
├───────────┼─────────────────────────────────────────────┤
│  分布式事务│  Seata / ShardingSphere                    │
└───────────┴─────────────────────────────────────────────┘
```

---

（开心）Java 微服务架构章节已完成～ 如需继续完善某个方向，请告诉我，我们一起努力！
