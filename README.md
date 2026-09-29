# 租房管理系统

基于 **uni-app 微信小程序 + Spring Boot** 的租房管理平台，支持租客找房与房东发布房源。毕业设计项目。

---

## 一、技术栈

| 层次   | 技术                  | 说明                                      |
| ------ | --------------------- | ----------------------------------------- |
| 前端   | uni-app（Vue 2 语法） | 一套代码编译到微信小程序                  |
| 后端   | Spring Boot 3.3.5     | 提供 RESTful 接口                         |
| ORM    | MyBatis-Plus 3.5.7    | 单表 CRUD 用 BaseMapper，复杂查询手写 XML |
| 数据库 | MySQL 8.0             | 字符集 utf8mb4                            |
| 鉴权   | JWT（jjwt 0.12.6）    | 无状态登录态                              |
| 构建   | Maven / HBuilderX     | 后端 Maven，前端 HBuilderX 或 CLI         |

---

## 二、功能特性

**租客端**

- 微信一键登录（code 换取 openid）
- 自动定位 / 手动选点，查看附近房源（按距离升序）
- 关键词搜索（标题、地址模糊匹配）
- 房源详情：图片轮播、房间信息、房东联系方式（可一键拨号）

**房东端**

- 身份切换（租客 / 房东）
- 发布房源：地址选点、大小、格局、租金、最多 9 张实拍图
- 我的房源管理：编辑、下架

**通用**

- 个人信息维护（姓名、性别、联系方式）
- 统一响应体与全局异常处理
- JWT 拦截器保护需登录接口

---

## 三、目录结构与文件说明

```
Graduation_Project/
├── pages/                      # 小程序页面
├── components/                 # 自定义组件
├── utils/                      # 工具模块
├── static/                     # 静态资源
├── servers/                    # Spring Boot 后端
├── App.vue / main.js / ...      # uni-app 应用级文件
└── README.md
```

### 3.1 前端（uni-app）

#### 应用级文件

| 文件                       | 作用                                                                                 |
| -------------------------- | ------------------------------------------------------------------------------------ |
| `App.vue`                  | 应用根组件，定义全局样式与 onLaunch/onShow 等应用生命周期                            |
| `main.js`                  | 应用入口，创建并挂载 Vue 实例                                                        |
| `pages.json`               | **页面路由注册表**：声明所有页面路径、导航栏标题、全局窗口样式。新增页面必须在此登记 |
| `manifest.json`            | 应用配置：小程序 appid、定位权限声明、`urlCheck: false`（开发期不校验合法域名）      |
| `uni.scss`                 | 全局 SCSS 变量，供各页面样式引用                                                     |
| `uni.promisify.adaptor.js` | 将 uni 的回调式 API 适配为 Promise 风格                                              |
| `index.html`               | H5 端的页面模板（编译到小程序时不使用）                                              |
| `package.json`             | 前端依赖与类型声明（仅 devDependencies，用于编辑器提示）                             |

#### 工具模块

| 文件               | 作用                                                                                                                                                                                                                                                                                                                         |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `utils/request.js` | **网络请求统一封装**。职责：① 拼接 baseURL；② 自动携带 JWT（`Authorization: Bearer`）；③ 解析后端统一响应体 `{code,msg,data}` 并弹错误提示；④ 未登录时跳转登录页。同时导出全部业务接口方法（`login` / `publishHouse` / `getNearbyHouses` 等）、图片地址拼接 `toFullUrl`、以及真机图片预下载 `preloadImage` / `preloadCovers` |

#### 页面

| 文件                                 | 作用                                                                                                               |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| `pages/login/login.vue`              | 登录页。调用 `uni.login` 拿 code → 请求后端 `/api/login` → 存 token 与用户信息 → 按身份跳转                        |
| `pages/role/role.vue`                | 身份选择页。首次登录时选择租客或房东，同步到后端                                                                   |
| `pages/home/home.vue`                | **租客首页**。获取定位后请求 `/api/house/nearby`，展示附近房源卡片与距离；含身份守卫（房东访问时重定向到房东主页） |
| `pages/search/search.vue`            | 搜索页。关键词搜索 + 选点搜索，结果按距离升序                                                                      |
| `pages/house/house.vue`              | 房源详情页。图片轮播、房间信息、房东信息与拨号                                                                     |
| `pages/mine/mine.vue`                | 「我的」页。展示用户信息、编辑资料入口、身份切换、退出登录                                                         |
| `pages/profile/profile.vue`          | 个人信息编辑页。读取/保存姓名、性别、联系方式                                                                      |
| `pages/landlord/home/home.vue`       | 房东中心。发布房源、我发布的房源两个入口；含身份守卫                                                               |
| `pages/landlord/publish/publish.vue` | **发布/编辑房源页**。地址选点、大小格局租金、图片选择与上传、房东信息；带 `?id=` 参数时为编辑模式（自动回填）      |
| `pages/landlord/list/list.vue`       | 我的房源列表。展示封面与状态，支持编辑、下架                                                                       |

#### 组件

| 文件                                   | 作用                                                              |
| -------------------------------------- | ----------------------------------------------------------------- |
| `components/tab-bar/tab-bar.vue`       | 底部导航栏，按当前角色动态渲染「首页 / 我的」，用 `reLaunch` 切换 |
| `components/detail-btn/detail-btn.vue` | 「查看详情」按钮组件，封装跳转到 `/pages/house/house?id=xx`       |

#### 静态资源

| 文件              | 作用                |
| ----------------- | ------------------- |
| `static/logo.png` | 应用图标 / 图片占位 |

### 3.2 后端（servers/）

#### 项目根文件

| 文件                 | 作用                                                                          |
| -------------------- | ----------------------------------------------------------------------------- |
| `pom.xml`            | Maven 依赖与构建配置：Spring Boot Web、MyBatis-Plus、MySQL 驱动、Lombok、jjwt |
| `servers/.gitignore` | 忽略构建产物 `target/`、IDE 文件、以及含密码的本地配置文件                    |

#### 部署文件 `deploy/`

| 文件                             | 作用                                                                                                                                                            |
| -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `deploy/rental-server.service`   | **systemd 服务单元**。托管后端进程：开机自启、崩溃后 5 秒自动拉起、SIGTERM 优雅停机、日志统一写入 `/var/log/rental-server/`。安装到 `/etc/systemd/system/`      |
| `deploy/rental-server.logrotate` | **日志轮换配置**。每天轮换、保留 14 份、历史日志 gzip 压缩；使用 `copytruncate` 截断而非重命名，确保 systemd 持有的文件句柄仍然有效。安装到 `/etc/logrotate.d/` |

#### 启动与配置

| 文件                                     | 作用                                                                                                             |
| ---------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `RentalServerApplication.java`           | **Spring Boot 启动类**。`@SpringBootApplication` + `@MapperScan("com.rental.server.mapper")` 扫描 Mapper 接口    |
| `resources/application.yml`              | **公共配置**：端口 8080、UTF-8 编码、文件上传大小限制、MyBatis-Plus 驼峰映射与自增主键、默认激活 `dev` 环境      |
| `resources/application-dev.yml.example`  | 开发环境配置**模板**（含数据库地址、图片目录、JWT 密钥、微信 appid/secret、mock 开关）。真实文件需自行复制并填值 |
| `resources/application-prod.yml.example` | 生产环境配置模板（服务器上的 MySQL 与图片目录）                                                                  |

> ⚠️ 真实的 `application-dev.yml` / `application-prod.yml` 含数据库密码与小程序 AppSecret，已在 `.gitignore` 中忽略，不会提交到仓库。

#### 通用模块 `common/`

| 文件                          | 作用                                                                                                                    |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `Result.java`                 | **统一响应体** `{code, msg, data}`。提供 `success()` / `error()` 静态工厂方法，所有接口统一返回该结构                   |
| `BusinessException.java`      | 业务异常类，用于主动抛出可预期错误（如"房源不存在""无权修改"），携带自定义 code                                         |
| `GlobalExceptionHandler.java` | **全局异常处理器**。`@RestControllerAdvice` 捕获业务异常、参数校验异常、未知异常，统一转为 `Result` 返回，避免 500 白页 |

#### 配置模块 `config/`

| 文件                  | 作用                                                                                                                                                             |
| --------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `WebConfig.java`      | Web MVC 配置。① **静态资源映射**：把本地图片目录暴露为 `/images/**` URL；② **注册 JWT 拦截器**：拦截 `/api/**`，放行登录、健康检查、房源详情/附近/搜索等游客接口 |
| `JwtInterceptor.java` | JWT 拦截器。校验请求头 `Authorization: Bearer <token>`，解析出 userId 存入 request attribute，后续 Controller 直接取用                                           |

#### 实体 `entity/`

| 文件         | 作用                                                                                                                    |
| ------------ | ----------------------------------------------------------------------------------------------------------------------- |
| `User.java`  | 用户实体，对应 `user` 表。租客与房东共用一张表，`role` 区分身份                                                         |
| `House.java` | 房源实体，对应 `house` 表。含两个非数据库字段：`imageList`（接收前端图片数组）、`distance`（附近查询时由 SQL 计算填充） |

#### 数据访问 `mapper/`

| 文件                               | 作用                                                                                                  |
| ---------------------------------- | ----------------------------------------------------------------------------------------------------- |
| `UserMapper.java`                  | 用户表 Mapper，继承 `BaseMapper<User>` 获得基础 CRUD                                                  |
| `HouseMapper.java`                 | 房源表 Mapper。除基础 CRUD 外，声明 `selectNearby`（附近查询）与 `search`（关键词搜索）两个自定义方法 |
| `resources/mapper/HouseMapper.xml` | **手写 SQL**。实现两阶段附近查询（矩形粗筛 + Haversine 精算）与关键词模糊搜索，是核心算法的落地点     |

#### 业务逻辑 `service/`

| 文件                | 作用                                                                                                                                                                |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `HouseService.java` | **房源业务逻辑**。① 发布/编辑/下架房源（含归属校验，禁止操作他人房源）；② 附近查询：计算矩形边界后调用 Mapper；③ 关键词搜索；④ 实体 ↔ VO 转换，解析图片 JSON 字符串 |

#### 接口层 `controller/`

| 文件                    | 作用                                                                                                                           |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `HealthController.java` | 健康检查接口 `/api/ping`，返回 user/house 条数，用于验证服务与数据库连通                                                       |
| `AuthController.java`   | **登录接口** `/api/login`。code → openid → 查/建用户 → 签发 JWT。含 mock 开关：开发期可直接用 code 当 openid，免去真实微信调用 |
| `UserController.java`   | 用户接口。`GET /api/user/me` 查当前用户、`PUT /api/user/me` 改资料（姓名/性别/联系方式/身份）                                  |
| `HouseController.java`  | **房源接口**。发布、我的列表、详情、编辑、下架、附近查询、关键词搜索                                                           |
| `UploadController.java` | 图片上传接口 `/api/upload`。校验扩展名、按 `yyyy/MM` 分目录存放、UUID 重命名，返回可直接访问的相对路径                         |

#### 工具与返回对象

| 文件                 | 作用                                                                                                          |
| -------------------- | ------------------------------------------------------------------------------------------------------------- |
| `utils/JwtUtil.java` | JWT 工具类。生成 token（payload 存 userId，默认 7 天有效）与解析 token                                        |
| `vo/HouseVO.java`    | 房源返回对象。在实体基础上补充 `images` 数组、`cover` 封面、`distance` 距离、房东姓名/电话/性别等前端所需字段 |

---

## 四、数据库设计

### user 表

| 字段        | 类型        | 说明                                   |
| ----------- | ----------- | -------------------------------------- |
| id          | BIGINT      | 主键，自增                             |
| openid      | VARCHAR(64) | 微信唯一标识，**唯一索引**（登录幂等） |
| role        | VARCHAR(16) | 身份：tenant=租客，landlord=房东       |
| name        | VARCHAR(32) | 姓名                                   |
| gender      | TINYINT     | 性别：1=男，2=女                       |
| contact     | VARCHAR(32) | 联系方式                               |
| create_time | DATETIME    | 注册时间                               |

### house 表

| 字段        | 类型          | 说明                                               |
| ----------- | ------------- | -------------------------------------------------- |
| id          | BIGINT        | 主键，自增                                         |
| user_id     | BIGINT        | 发布人，关联 user.id，普通索引                     |
| title       | VARCHAR(64)   | 房源标题                                           |
| address     | VARCHAR(255)  | 详细地址                                           |
| lat / lng   | DECIMAL(10,6) | 经纬度，**联合索引 idx_lat_lng**（附近查询粗筛用） |
| area        | DECIMAL(8,2)  | 面积（㎡）                                         |
| layout      | VARCHAR(32)   | 格局，如"2室1厅"                                   |
| price       | INT           | 租金（元/月）                                      |
| images      | TEXT          | 图片相对路径 JSON 数组                             |
| status      | TINYINT       | 1=上架，0=下架（软删除）                           |
| create_time | DATETIME      | 发布时间                                           |

建表 SQL 见 [`servers/sql/schema.sql`](servers/sql/schema.sql)，可直接导入执行。

---

## 五、接口清单

| 方法   | 路径                     | 说明                             | 是否需登录 |
| ------ | ------------------------ | -------------------------------- | ---------- |
| GET    | `/api/ping`              | 健康检查                         | 否         |
| POST   | `/api/login`             | 微信登录，返回 token + 用户信息  | 否         |
| GET    | `/api/user/me`           | 获取当前用户                     | 是         |
| PUT    | `/api/user/me`           | 更新当前用户资料                 | 是         |
| POST   | `/api/upload`            | 上传图片，返回相对路径           | 是         |
| POST   | `/api/house`             | 发布房源                         | 是         |
| GET    | `/api/house/mine`        | 我发布的房源                     | 是         |
| GET    | `/api/house/detail/{id}` | 房源详情（含房东信息）           | 否         |
| PUT    | `/api/house/{id}`        | 编辑房源                         | 是         |
| DELETE | `/api/house/{id}`        | 下架房源（软删除）               | 是         |
| GET    | `/api/house/nearby`      | 附近房源，参数 lat/lng/radius    | 否         |
| GET    | `/api/house/search`      | 关键词搜索，参数 keyword/lat/lng | 否         |

---

## 六、核心算法：附近房源查询

采用**两阶段查询**策略（见 `mapper/HouseMapper.xml`）：

**第一阶段 · 矩形粗筛**
先用经纬度范围快速排除远处数据，充分利用 `idx_lat_lng` 联合索引：

- 纬度方向：1 度 ≈ 111 km，故 `Δlat = radius / 111`
- 经度方向：1 度的实际距离随纬度升高而缩短，`Δlng = radius / (111 × cos(lat))`

```sql
WHERE lat BETWEEN ? AND ? AND lng BETWEEN ? AND ?
```

**第二阶段 · Haversine 精算**
对粗筛结果计算球面距离，过滤并排序：

```
d = 6371 × acos( cos(rad(φ₁))·cos(rad(φ₂))·cos(rad(λ₂)−rad(λ₁)) + sin(rad(φ₁))·sin(rad(φ₂)) )
```

用 `LEAST(1, GREATEST(-1, ...))` 把 `acos` 入参限制在 `[-1,1]`，避免浮点误差导致结果为 NaN。

> 为什么不直接用 Haversine？全表逐行计算函数无法走索引，数据量大时性能急剧下降。先用索引粗筛把候选集缩小，再精算，是本设计的优化点。

---

## 七、本地运行

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0
- HBuilderX 或微信开发者工具

### 1. 初始化数据库

执行 [`servers/sql/schema.sql`](servers/sql/schema.sql) 创建数据库与两张表：

```bash
mysql -u root -p < servers/sql/schema.sql
```

或直接在 MySQL Workbench / Navicat 中打开该文件执行。

### 2. 配置后端

```bash
cd servers/src/main/resources
cp application-dev.yml.example application-dev.yml
```

编辑 `application-dev.yml`，填入你的 MySQL 密码、小程序 AppID 与 AppSecret。

### 3. 启动后端

```bash
cd servers
mvn spring-boot:run
```

或用 IntelliJ IDEA 打开 `servers` 目录，运行 `RentalServerApplication`。

验证：浏览器访问 http://localhost:8080/api/ping ，返回 `{"code":200,...}` 即成功。

### 4. 启动前端

用 HBuilderX 打开项目根目录 → 运行 → 运行到小程序模拟器 → 微信开发者工具。

**注意**：真机调试时需把 `utils/request.js` 中的 `BASE_URL` 改为电脑的局域网 IP（如 `http://192.168.1.102:8080`），并确保手机与电脑在同一 WiFi。

---

## 八、部署到服务器

采用 **systemd 托管 + logrotate 日志轮换** 的生产级部署方式（不使用 `nohup`，以获得开机自启、崩溃自动拉起与优雅停机能力）。

### 1. 环境准备

```bash
# 安装 JDK 17（MySQL 8 需已安装并创建好 userdb 与账号）
sudo apt update && sudo apt install -y openjdk-17-jre-headless

# 创建应用目录、日志目录、图片目录
sudo mkdir -p /opt/rental-server /var/log/rental-server /data/user/images
```

### 2. 打包并上传

```bash
# 本地打包
mvn clean package -DskipTests

# 上传 jar 到服务器（在本地执行）
scp servers/target/rental-server-0.0.1-SNAPSHOT.jar root@服务器IP:/root/

# 服务器上放入应用目录
sudo cp /root/rental-server-0.0.1-SNAPSHOT.jar /opt/rental-server/
```

> 如需修改数据库密码等生产配置，可复制 `application-prod.yml.example` 为 `application-prod.yml` 放到 `/opt/rental-server/`，Spring Boot 会自动读取 jar 同目录的配置并覆盖 jar 内的同名配置，无需重新打包。

### 3. 安装 systemd 服务与日志轮换

```bash
# 上传 servers/deploy/ 下的两个文件到服务器后执行
sudo cp /root/rental-server.service /etc/systemd/system/
sudo cp /root/rental-server.logrotate /etc/logrotate.d/rental-server
sudo chmod 644 /etc/logrotate.d/rental-server

sudo systemctl daemon-reload
sudo systemctl enable rental-server    # 开机自启
sudo systemctl start rental-server
```

### 4. 验证

```bash
sudo systemctl status rental-server
curl http://localhost:8080/api/ping
tail -f /var/log/rental-server/rental-server.log

# 演练日志轮换配置（-d 只演示不执行）
sudo logrotate -d /etc/logrotate.d/rental-server
```

### 5. 前端指向服务器

把 `utils/request.js` 中的 `BASE_URL` 改为服务器地址，重新编译小程序。

### 常用管理命令

| 操作       | 命令                                               |
| ---------- | -------------------------------------------------- |
| 查看状态   | `sudo systemctl status rental-server`              |
| 重启服务   | `sudo systemctl restart rental-server`             |
| 停止服务   | `sudo systemctl stop rental-server`                |
| 实时看日志 | `tail -f /var/log/rental-server/rental-server.log` |
| 历史日志   | `ls -lh /var/log/rental-server/`                   |

---

## 九、开发注意事项

- **敏感信息**：`application-dev.yml`、`application-prod.yml`、`AppSecret.txt` 已在 `.gitignore` 中忽略，请勿提交
- **新增页面**：必须在 `pages.json` 中注册，否则无法跳转
- **接口返回**：统一使用 `Result` 包装，Controller 不直接返回实体
- **真机图片**：小程序真机上 `<image>` 直接加载 HTTP 明文图片会被拦截，本项目通过 `preloadCovers` 先 `downloadFile` 到本地临时路径来规避；正式环境建议改用 HTTPS
