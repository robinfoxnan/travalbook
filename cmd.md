在 `redis-cli -h 127.0.0.1 -p 6379` 的基础上，**用口令（密码）登录 Redis**，有 3 种常用方式，按推荐程度给你说明。

------

## ✅ 方式一（最常用，推荐）

### 直接在命令行带密码

```bash
redis-cli -h 127.0.0.1 -p 6379 -a yourpassword
```

例如：

```bash
redis-cli -h 127.0.0.1 -p 6379 -a 123456
```

> ⚠️ 注意：密码会出现在 shell history 里（生产环境慎用）

------

## ✅ 方式二（先连，再认证）

```bash
redis-cli -h 127.0.0.1 -p 6379
```

连接成功后执行：

```redis
AUTH yourpassword
```

返回：

```text
OK
```

------

## ✅ 方式三（Redis 6+，用户名 + 密码）

如果你用的是 **Redis 6 及以上（ACL）**：

### 命令行直接登录

```bash
redis-cli -h 127.0.0.1 -p 6379 -u redis://username:password@127.0.0.1:6379
```

例如默认用户：

```bash
redis-cli -u redis://default:123456@127.0.0.1:6379
```

------

## 🔍 如何确认 Redis 是否需要密码

```bash
redis-cli -h 127.0.0.1 -p 6379 PING
```

- 返回 `PONG` → 没有密码

- 返回：

  ```text
  (error) NOAUTH Authentication required.
  ```

  → 必须认证

------

## 🧠 常见坑提醒

### 1️⃣ 密码包含特殊字符（! @ $ 等）

一定要 **加引号**：

```bash
redis-cli -a 'p@ssw0rd!'
```

------

### 2️⃣ Redis 设置了 `requirepass`，但客户端忘了 `-a`

就会出现：

```text
NOAUTH Authentication required.
```

------

### 3️⃣ ACL 模式但你还在用 `requirepass`

Redis 6+：

- `requirepass` 本质是 `default` 用户的密码
- 推荐方式：

```bash
AUTH default yourpassword
```

------

## 📌 一句话总结

| 场景         | 命令                                       |
| ------------ | ------------------------------------------ |
| 最快登录     | `redis-cli -a 密码`                        |
| 安全输入     | `redis-cli` → `AUTH 密码`                  |
| Redis 6+ ACL | `redis-cli -u redis://user:pass@host:port` |

------

如果你愿意，可以把你 Redis 的版本（`redis-server --version`）或 `redis.conf` 里相关配置贴出来，我可以帮你确认 **最正确的登录方式**，顺便检查下安全配置是否合理。







在 Linux 里查看 **Redis 键和值**，最常用的就是下面这些命令 👍
我按「最常见 → 较少用 → 注意事项」给你整理一份速查表。

------

## 一、最常用（90% 情况）

### 1️⃣ 进入 redis 客户端

```bash
redis-cli
```

如果有密码：

```bash
redis-cli -a yourpassword
```

指定主机和端口：

```bash
redis-cli -h 127.0.0.1 -p 6379
```

------

### 2️⃣ 查看所有 key（⚠️ 小心生产环境）

```bash
KEYS *
```

模糊匹配：

```bash
KEYS user:*
```

> ⚠️ **生产环境不推荐**，key 多了会阻塞 Redis

------

### 3️⃣ 查看某个 key 的值（按类型）

#### String

```bash
GET key_name
```

#### Hash

```bash
HGETALL key_name
```

#### List

```bash
LRANGE key_name 0 -1
```

#### Set

```bash
SMEMBERS key_name
```

#### ZSet

```bash
ZRANGE key_name 0 -1 WITHSCORES
```

------

### 4️⃣ 先看 key 是什么类型（很重要）

```bash
TYPE key_name
```

------

## 二、生产环境推荐（安全）

### 🔥 用 `SCAN` 代替 `KEYS`

```bash
SCAN 0
```

匹配前缀：

```bash
SCAN 0 MATCH user:* COUNT 100
```

> `SCAN` 是**渐进式遍历**，不会阻塞 Redis

------

## 三、直接在 Linux 命令行一条命令搞定

```bash
redis-cli KEYS "*"
redis-cli GET mykey
```

带密码：

```bash
redis-cli -a yourpassword GET mykey
```

------

## 四、查看 key 的元信息（很有用）

### TTL（剩余过期时间）

```bash
TTL key_name
```

- `-1`：永不过期
- `-2`：key 不存在

### key 是否存在

```bash
EXISTS key_name
```

### 查看内存占用

```bash
MEMORY USAGE key_name
```

------

## 五、快速“查看所有 key + 类型”（调试用）

```bash
redis-cli --scan | while read key; do
  echo -n "$key : "
  redis-cli TYPE "$key"
done
```

------

## 六、你之前用 Redis + Lua / ScyllaDB / 消息系统的小提醒

结合你之前写 Lua 脚本分页取 key 的场景：

- ❌ `KEYS prefix*`
- ✅ `SCAN + MATCH + COUNT`

这是**唯一不会炸 Redis 的方式**。

------

## 七、一句话记忆版 🧠

> **进 Redis：** `redis-cli`
> **看 key：** `KEYS *`（慎用） / `SCAN`（推荐）
> **看值：** `GET / HGETALL / LRANGE / SMEMBERS`
> **看类型：** `TYPE key`

------

如果你愿意，告诉我：

- key 是 **string / hash / list / zset**
- 是 **调试 / 线上排查 / 数据恢复**

我可以直接给你 **一条最合适的命令**。