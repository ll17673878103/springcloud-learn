---
trigger: manual
---

# JAVA_KNOWLEDGE.md - Java 知识库：小镜花的"九阴真经"

## 说明

这份知识库涵盖了 Java 领域的核心知识点。如同武林秘籍，我会将每一项整理成清晰、可操作的条目。

（微微低头）坦白说……Java 的知识体系很庞大，我会尽力确保准确。请务必，对于重要的技术决策，还是要查阅 Oracle 官方文档确认。

---

## 目录索引

| 章节 | 内容 | 状态 |
|------|------|------|
| [1. Java 基础](#1-java-基础) | 语法、数据类型、关键字 | ✅ 完成 |
| [2. 面向对象](#2-面向对象) | 类、继承、多态、接口 | ✅ 完成 |
| [3. 集合框架](#3-集合框架) | List、Set、Map、Queue | ✅ 完成 |
| [4. 异常处理](#4-异常处理) | 异常体系、捕获、抛出 | ✅ 完成 |
| [5. 泛型与反射](#5-泛型与反射) | 泛型擦除、Class对象 | ✅ 完成 |
| [6. 多线程与并发](#6-多线程与并发) | 线程、安全、锁、JUC | 🔨 完善中 |
| [7. JVM](#7-jvm) | 内存模型、GC、类加载 | 🔨 完善中 |
| [8. IO/NIO](#8-ionio) | 字节流、字符流、Channel | 🔨 完善中 |
| [9. 新特性](#9-新特性) | Java 8~21 新特性 | 🔨 完善中 |

---

## 1. Java 基础

### 1.1 数据类型

**基本类型（8种）**：

| 类型 | 字节 | 取值范围 | 默认值 |
|------|------|----------|--------|
| byte | 1 | -128 ~ 127 | 0 |
| short | 2 | -32768 ~ 32767 | 0 |
| int | 4 | -2³¹ ~ 2³¹-1 | 0 |
| long | 8 | -2⁶³ ~ 2⁶³-1 | 0L |
| float | 4 | ±3.4e38 | 0.0f |
| double | 8 | ±1.7e308 | 0.0d |
| char | 2 | 0 ~ 65535 | '\u0000' |
| boolean | 1 | true/false | false |

**引用类型**：
- 类、接口、数组、枚举
- 默认值都是 `null`

### 1.2 关键字

| 类别 | 关键字 |
|------|--------|
| 访问修饰符 | `public`, `protected`, `private`, `default` |
| 类相关 | `class`, `interface`, `extends`, `implements`, `abstract`, `static`, `final` |
| 方法相关 | `void`, `return`, `this`, `super` |
| 流程控制 | `if`, `else`, `for`, `while`, `do`, `switch`, `case`, `break`, `continue` |
| 异常 | `try`, `catch`, `finally`, `throw`, `throws` |
| 其他 | `new`, `instanceof`, `import`, `package`, `true`, `false`, `null` |

### 1.3 字符串处理

```java
// String（不可变）
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

s1 == s2  // true（常量池）
s1 == s3  // false（堆内存）
s1.equals(s3)  // true（内容比较）

// StringBuilder（可变，非线程安全，效率高）
StringBuilder sb = new StringBuilder();
sb.append("hello").append(" world");

// StringBuffer（可变，线程安全，效率低）
StringBuffer sb2 = new StringBuffer();
sb2.append("hello").append(" world");
```

**字符串拼接**：
- `+` 运算符：编译时优化为 StringBuilder
- `StringBuilder.append()`：推荐
- `String.concat()`：适合少量拼接
- `String.join()`：适合多个字符串拼接

### 1.4 自动装箱与拆箱

```java
// 自动装箱：基本类型 → 包装类型
Integer i = 10;  // Integer.valueOf(10)

// 自动拆箱：包装类型 → 基本类型
int j = i;  // i.intValue()

// 注意：装箱有缓存（-128 ~ 127）
Integer a = 127;
Integer b = 127;
a == b  // true（缓存）

Integer c = 128;
Integer d = 128;
c == d  // false（超出缓存）
```

---

## 2. 面向对象

### 2.1 类与对象

```java
public class User {
    // 属性
    private String name;
    private int age;

    // 构造方法
    public User() {}

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

### 2.2 封装、继承、多态

**封装**：
- 将数据和对数据的操作封装在类内部
- 通过访问修饰符控制访问级别
- 提供 getter/setter 方法

**继承**：
```java
public class Animal {
    protected String name;
    public void eat() { }
}

public class Dog extends Animal {
    private String breed;
    
    @Override
    public void eat() {  // 重写父类方法
        super.eat();  // 调用父类方法
    }
}
```

**多态**：
```java
// 父类引用指向子类对象
Animal animal = new Dog();

// 编译时多态：方法重载
public int add(int a, int b) { return a + b; }
public int add(int a, int b, int c) { return a + b + c; }

// 运行时多态：方法重写
animal.eat();  // 调用的是 Dog 的 eat()
```

### 2.3 接口与抽象类

| 区别 | 接口 | 抽象类 |
|------|------|--------|
| 关键字 | `interface` | `abstract class` |
| 方法 | JDK7：抽象方法；JDK8+：default/static | 抽象/普通方法 |
| 属性 | 只能是 `public static final` | 任意修饰符 |
| 关系 | 类可以实现多个接口 | 类只能继承一个抽象类 |
| 构造方法 | 不能有 | 可以有 |
| 使用场景 | 行为契约 | 模板复用 |

```java
// 接口
public interface Flyable {
    int MAX_SPEED = 1000;  // public static final
    void fly();  // public abstract
}

// JDK8+ 接口
public interface Flyable {
    default void land() { }  // 默认实现
    
    static void test() { }  // 静态方法
}

// 实现
public class Bird implements Flyable {
    @Override
    public void fly() { }
}
```

### 2.4 内部类

```java
public class Outer {
    private int x = 10;

    // 成员内部类
    public class Inner {
        public void method() {
            System.out.println(x);  // 可以访问外部类成员
        }
    }

    // 静态内部类
    public static class StaticInner {
        // 只能访问外部类静态成员
    }

    // 局部内部类
    public void method() {
        class LocalInner {
            int y = 20;
        }
    }
}

// 匿名内部类
Runnable r = new Runnable() {
    @Override
    public void run() { }
};
```

---

## 3. 集合框架

### 3.1 架构总览

```
Collection
├── List（有序、可重复）
│   ├── ArrayList（数组，查询快，增删慢）
│   ├── LinkedList（链表，增删快，查询慢）
│   └── Vector（数组，线程安全）
├── Set（无序、去重）
│   ├── HashSet（哈希表，无序）
│   ├── LinkedHashSet（链表+哈希，有序）
│   └── TreeSet（红黑树，排序）
└── Queue（队列）
    ├── LinkedList
    ├── PriorityQueue（优先级队列）
    └── Deque（双端队列）
        └── ArrayDeque

Map（键值对）
├── HashMap（哈希表）
├── LinkedHashMap（链表+哈希）
├── TreeMap（红黑树）
├── Hashtable（线程安全）
└── ConcurrentHashMap（分段锁）
```

### 3.2 ArrayList vs LinkedList

| 操作 | ArrayList | LinkedList |
|------|-----------|------------|
| 随机访问 | O(1) | O(n) |
| 头部插入/删除 | O(n) | O(1) |
| 尾部插入/删除 | O(1)（扩容时 O(n)） | O(1) |
| 内存占用 | 连续内存 | 节点分散 |
| 适用场景 | 频繁随机访问 | 频繁增删操作 |

### 3.3 HashMap 核心原理

**JDK 7 vs JDK 8+**：
- JDK 7：数组 + 链表
- JDK 8+：数组 + 链表 + 红黑树（链表长度 > 8 时转换）

**put 流程**：
1. 计算 key 的 hash 值
2. 通过 `(n - 1) & hash` 计算索引位置
3. 如果该位置为空，直接插入
4. 如果不为空，遍历链表或红黑树
5. 如果 key 已存在，覆盖 value
6. 如果超过阈值，扩容

**扩容机制**：
- 默认容量 16，负载因子 0.75
- 扩容时容量翻倍
- 重新计算所有元素的索引位置

### 3.4 常用集合操作

```java
// 创建与添加
List<String> list = new ArrayList<>();
list.add("a");
list.add(0, "b");  // 插入到指定位置

// 遍历
for (String s : list) { }
list.forEach(System.out::println);

// 删除
list.remove("a");
list.remove(0);

// Map 操作
Map<String, Integer> map = new HashMap<>();
map.put("a", 1);
map.get("a");           // 获取
map.containsKey("a");   // 是否包含
map.remove("a");        // 删除
map.forEach((k, v) -> System.out.println(k + ":" + v));

// Stream 操作
list.stream()
    .filter(s -> s.startsWith("a"))
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

---

## 4. 异常处理

### 4.1 异常体系

```
Throwable
├── Error（错误，程序无法处理）
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── ...
└── Exception（异常）
    ├── RuntimeException（运行时异常，可处理可不处理）
    │   ├── NullPointerException
    │   ├── ArrayIndexOutOfBoundsException
    │   ├── ClassCastException
    │   └── ArithmeticException
    └── 非运行时异常（Checked Exception，必须处理）
        ├── IOException
        ├── SQLException
        ├── FileNotFoundException
        └── ...
```

### 4.2 异常处理

```java
// try-catch-finally
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("除数不能为零");
    e.printStackTrace();  // 打印异常堆栈
} finally {
    // 无论是否异常都会执行
    // 用于释放资源
    System.out.println("最终执行");
}

// 多异常捕获
try {
    // ...
} catch (IOException | SQLException e) {
    // JDK 7+ 支持
    e.printStackTrace();
}

// throws vs throw
public void method() throws IOException {  // 声明抛出
    throw new IOException("文件不存在");  // 抛出异常
}
```

### 4.3 自定义异常

```java
public class BusinessException extends RuntimeException {
    private int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}

// 使用
throw new BusinessException(1001, "用户不存在");
```

---

## 5. 泛型与反射

### 5.1 泛型

**泛型类**：
```java
public class Box<T> {
    private T value;
    
    public T getValue() {
        return value;
    }
    
    public void setValue(T value) {
        this.value = value;
    }
}

// 使用
Box<String> stringBox = new Box<>();
stringBox.setValue("hello");
String value = stringBox.getValue();
```

**泛型方法**：
```java
public static <T> T getMiddle(T... array) {
    return array[array.length / 2];
}

String middle = getMiddle("a", "b", "c");
```

**泛型限定**：
```java
// 上界限定：T 必须是 Number 或其子类
public <T extends Number> void test(T t) { }

// 下界限定：T 必须是 Integer 或其父类
<? super Integer>

// 多重限定
<T extends Comparable & Serializable>
```

**泛型擦除**：
- 编译时进行类型检查
- 运行时类型信息被擦除
- 泛型信息存储在 Class 字节码的 Signature 属性中

### 5.2 反射

**获取 Class 对象**：
```java
// 方式一：Class.forName()
Class<?> clazz = Class.forName("com.example.User");

// 方式二：.class
Class<User> clazz = User.class;

// 方式三：getClass()
User user = new User();
Class<? extends User> clazz = user.getClass();
```

**反射操作**：
```java
// 创建实例
User user = (User) clazz.getDeclaredConstructor().newInstance();

// 获取构造方法
Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, int.class);
User user2 = (User) constructor.newInstance("Alice", 25);

// 获取字段
Field nameField = clazz.getDeclaredField("name");
nameField.setAccessible(true);  // 设置访问权限
String name = (String) nameField.get(user);
nameField.set(user, "Bob");

// 获取方法
Method setNameMethod = clazz.getDeclaredMethod("setName", String.class);
setNameMethod.invoke(user, "Charlie");

// 获取父类/接口
Class<?> superClass = clazz.getSuperclass();
Class<?>[] interfaces = clazz.getInterfaces();
```

---

## 6. 多线程与并发

### 6.1 线程基础

**创建线程**：
```java
// 方式一：继承 Thread
public class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running");
    }
}
new MyThread().start();

// 方式二：实现 Runnable
public class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running");
    }
}
new Thread(new MyRunnable()).start();

// 方式三：Lambda（推荐）
new Thread(() -> System.out.println("Lambda thread")).start();
```

**线程生命周期**：
```
NEW → RUNNABLE → BLOCKED/WAITING → TERMINATED
     (就绪/运行)  (阻塞/等待)
```

**常用方法**：
```java
thread.start()        // 启动线程
thread.join()         // 等待线程结束
thread.sleep(ms)      // 休眠（不释放锁）
thread.wait()         // 等待（释放锁）
thread.notify()       // 唤醒
thread.notifyAll()    // 唤醒所有
Thread.yield()        // 让出 CPU
```

### 6.2 Synchronized

```java
// 修饰方法（锁对象是 this）
public synchronized void method() { }

// 修饰静态方法（锁对象是类对象）
public static synchronized void staticMethod() { }

// 修饰代码块（锁对象是指定对象）
public void blockMethod() {
    synchronized (this) {
        // 同步代码
    }
}
```

**可重入性**：
- synchronized 是可重入锁
- 同一线程可以多次获取同一把锁
- 防止死锁

### 6.3 volatile

```java
public class VolatileDemo {
    // 保证可见性：修改后立即刷新到主内存
    // 不保证原子性
    // 禁止指令重排序
    private volatile boolean flag = false;
}
```

**volatile vs synchronized**：
| 特性 | volatile | synchronized |
|------|----------|--------------|
| 可见性 | ✅ | ✅ |
| 原子性 | ❌ | ✅ |
| 性能 | 高 | 低 |

### 6.4 JUC 并发包

**ConcurrentHashMap**：
```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.putIfAbsent("a", 1);  // 不存在才插入
map.computeIfAbsent("b", k -> 1);  // 不存在才计算
```

**CountDownLatch**：
```java
CountDownLatch latch = new CountDownLatch(3);

new Thread(() -> { /* 任务1 */ latch.countDown(); }).start();
new Thread(() -> { /* 任务2 */ latch.countDown(); }).start();
new Thread(() -> { /* 任务3 */ latch.countDown(); }).start();

latch.await();  // 等待所有任务完成
System.out.println("所有任务完成");
```

**CyclicBarrier**：
```java
CyclicBarrier barrier = new CyclicBarrier(3, () -> {
    System.out.println("所有线程都到达屏障，执行汇总");
});

new Thread(() -> { barrier.await(); }).start();
new Thread(() -> { barrier.await(); }).start();
new Thread(() -> { barrier.await(); }).start();
```

**Semaphore**：
```java
Semaphore semaphore = new Semaphore(2);

semaphore.acquire();  // 获取许可
try {
    // 访问共享资源
} finally {
    semaphore.release();  // 释放许可
}
```

**CompletableFuture**：
```java
CompletableFuture.supplyAsync(() -> {
    // 异步任务
    return "result";
}).thenApply(result -> {
    // 处理结果
    return result.toUpperCase();
}).thenAccept(System.out::println);
```

### 6.5 线程池

```java
// 创建线程池
ExecutorService executor = Executors.newFixedThreadPool(5);

// 提交任务
executor.execute(() -> {
    System.out.println("Task executed");
});

Future<String> future = executor.submit(() -> {
    return "Hello";
});
String result = future.get();  // 阻塞获取结果

executor.shutdown();  // 关闭线程池
```

**线程池参数**：
```java
new ThreadPoolExecutor(
    corePoolSize,      // 核心线程数
    maximumPoolSize,    // 最大线程数
    keepAliveTime,      // 空闲线程存活时间
    TimeUnit,           // 时间单位
    workQueue,           // 任务队列
    threadFactory,      // 线程工厂
    handler             // 拒绝策略
);
```

**拒绝策略**：
- `AbortPolicy`：抛出 RejectedExecutionException
- `CallerRunsPolicy`：由调用线程执行
- `DiscardPolicy`：丢弃任务
- `DiscardOldestPolicy`：丢弃最旧任务

---

## 7. JVM

### 7.1 内存区域

```
JVM 内存
├── 程序计数器       // 记录当前线程执行的字节码行号
├── 虚拟机栈        // 方法调用栈帧
│   └── 局部变量表、操作数栈、动态链接、方法返回地址
├── 本地方法栈      // Native 方法栈
├── 堆（Heap）      // 对象实例、数组
│   ├── 新生代
│   │   ├── Eden 区
│   │   └── Survivor 区（S0、S1）
│   └── 老年代
└── 方法区（MetaSpace）// 类信息、常量、静态变量
    └── 运行时常量池
```

### 7.2 垃圾回收

**GC 算法**：
- **标记-清除**：效率低，产生碎片
- **复制**：适用于新生代（Eden → Survivor）
- **标记-整理**：适用于老年代

**分代收集**：
- 新生代：Minor GC，频率高，对象存活率低
- 老年代：Major/Full GC，频率低

**常见 GC 收集器**：
| 收集器 | 特点 |
|--------|------|
| Serial | 单线程，最简单 |
| ParNew | 多线程版 Serial |
| Parallel Scavenge | 吞吐量优先 |
| CMS | 并发标记清除，低停顿 |
| G1 | 面向局部，平衡吞吐和停顿 |
| ZGC | 超低停顿（毫秒级） |

### 7.3 类加载机制

**类加载过程**：
```
加载 → 验证 → 准备 → 解析 → 初始化 → 使用 → 卸载
```

**类加载器**：
- Bootstrap ClassLoader：C++ 实现，加载 `JAVA_HOME/lib`
- Extension ClassLoader：加载 `JAVA_HOME/lib/ext`
- App ClassLoader：加载 classpath 指定目录

**双亲委派模型**：
- 类加载请求向上传递
- 优先使用父类加载器
- 保证类的唯一性和安全性

---

## 8. IO/NIO

### 8.1 IO 体系

```
字节流
├── InputStream
│   ├── FileInputStream
│   ├── BufferedInputStream
│   └── ObjectInputStream
└── OutputStream
    ├── FileOutputStream
    ├── BufferedOutputStream
    └── ObjectOutputStream

字符流
├── Reader
│   ├── FileReader
│   ├── BufferedReader
│   └── InputStreamReader
└── Writer
    ├── FileWriter
    ├── BufferedWriter
    └── OutputStreamWriter
```

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

// 字符流读写
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

// 对象序列化
try (ObjectOutputStream oos = new ObjectOutputStream(
         new FileOutputStream("user.dat"))) {
    oos.writeObject(new User("Alice", 25));
}

try (ObjectInputStream ois = new ObjectInputStream(
         new FileInputStream("user.dat"))) {
    User user = (User) ois.readObject();
}
```

### 8.3 NIO

**核心组件**：
- **Channel**：通道，类似流，但可双向读写
- **Buffer**：缓冲区，用于读写数据
- **Selector**：选择器，用于多路复用

```java
// NIO 文件读取
RandomAccessFile file = new RandomAccessFile("test.txt", "rw");
FileChannel channel = file.getChannel();

ByteBuffer buffer = ByteBuffer.allocate(1024);
channel.read(buffer);

buffer.flip();
while (buffer.hasRemaining()) {
    System.out.print((char) buffer.get());
}

channel.close();
file.close();
```

**Buffer 常用方法**：
```java
buffer.put()    // 写入
buffer.get()    // 读取
buffer.flip()   // 切换读写模式
buffer.clear()  // 清空缓冲区
buffer.compact() // 压缩已读数据
buffer.rewind() // 重置位置
```

---

## 9. 新特性

### 9.1 Java 8

**Lambda 表达式**：
```java
// 完整写法
Comparator<String> c = (String a, String b) -> { return a.compareTo(b); };

// 简化写法
Comparator<String> c = (a, b) -> a.compareTo(b);

// 方法引用
List<String> list = Arrays.asList("a", "b", "c");
list.forEach(System.out::println);
```

**Stream API**：
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// 过滤、映射、收集
List<Integer> result = numbers.stream()
    .filter(n -> n % 2 == 0)  // 过滤偶数
    .map(n -> n * 2)          // 乘以2
    .collect(Collectors.toList());

// 聚合操作
int sum = numbers.stream().mapToInt(Integer::intValue).sum();
Optional<Integer> max = numbers.stream().max(Integer::compareTo);
```

**接口默认方法**：
```java
public interface MyInterface {
    default void defaultMethod() {
        System.out.println("默认实现");
    }
}
```

**Optional**：
```java
Optional<String> optional = Optional.ofNullable(getName());

// 安全取值
String name = optional.orElse("默认值");
String name2 = optional.orElseGet(() -> computeDefault());

// 链式调用
optional.map(String::toUpperCase)
        .filter(s -> s.length() > 3)
        .ifPresent(System.out::println);
```

**日期时间 API**：
```java
LocalDate today = LocalDate.now();
LocalDateTime now = LocalDateTime.now();
LocalTime time = LocalTime.of(10, 30);

// 格式化
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formatted = now.format(formatter);
```

### 9.2 Java 11+

**String 增强**：
```java
"   hello   ".isBlank();      // true
"hello".repeat(3);            // "hellohellohello"
"hello\nworld".lines();       // Stream<String>
```

**文件操作简化**：
```java
// 读取文件所有行
List<String> lines = Files.readAllLines(Path.of("file.txt"));

// 写入文件
Files.writeString(Path.of("file.txt"), "content");
```

**局部变量类型推断**：
```java
var list = new ArrayList<String>();  // Java 10+
var stream = list.stream();
```

### 9.3 Java 17+

**sealed class（密封类）**：
```java
public sealed class Shape permits Circle, Rectangle, Triangle { }

// 允许的子类
public final class Circle extends Shape { }
public sealed class Rectangle extends Shape permits Square { }
public non-sealed class Triangle extends Shape { }
```

**Pattern Matching for switch**：
```java
static String formatter(Object obj) {
    return switch (obj) {
        case Integer i -> String.format("int %d", i);
        case String s -> String.format("String %s", s);
        case null, default -> "unknown";
    };
}
```

---

（微微欠身）Java 知识库基础部分已完成。高级特性与框架见下方章节～

---

## 10. 设计模式（GoF 23种）

### 10.1 创建型模式

**单例模式（Singleton）**：
```java
// 饿汉式（线程安全，但可能浪费资源）
public class Singleton1 {
    private static final Singleton1 INSTANCE = new Singleton1();
    private Singleton1() {}
    public static Singleton1 getInstance() {
        return INSTANCE;
    }
}

// 懒汉式（线程不安全）
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

// 双重检查锁（线程安全，推荐）
public class Singleton3 {
    private static volatile Singleton3 instance;
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

// 静态内部类（线程安全，推荐）
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

**工厂方法模式（Factory Method）**：
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

**抽象工厂模式（Abstract Factory）**：
```java
public interface AbstractFactory {
    ProductA createProductA();
    ProductB createProductB();
}

public class ConcreteFactory1 implements AbstractFactory {
    @Override
    public ProductA createProductA() {
        return new ProductA1();
    }
    @Override
    public ProductB createProductB() {
        return new ProductB1();
    }
}
```

**建造者模式（Builder）**：
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

// 使用
User user = new User.Builder()
    .name("Alice")
    .age(25)
    .address("Beijing")
    .build();
```

### 10.2 结构型模式

**适配器模式（Adapter）**：
```java
// 类适配器（继承）
public class ClassAdapter extends Adaptee implements Target {
    @Override
    public void request() {
        specificRequest();
    }
}

// 对象适配器（组合）
public class ObjectAdapter implements Target {
    private Adaptee adaptee;
    
    public ObjectAdapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
    
    @Override
    public void request() {
        adaptee.specificRequest();
    }
}
```

**装饰器模式（Decorator）**：
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

public class Decorator implements Component {
    protected Component component;
    
    public Decorator(Component component) {
        this.component = component;
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

（微微欠身）Java 微服务架构章节已完成～ 如需继续完善某个方向，请告诉我～
