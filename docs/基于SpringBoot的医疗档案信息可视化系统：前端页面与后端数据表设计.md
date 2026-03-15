# 基于SpringBoot的医疗档案信息可视化系统：前端页面与后端数据表设计

本文档严格贴合开题报告的业务需求、技术选型与角色划分，完成**可直接落地开发**的后端MySQL业务表设计，以及基于Vue.js的分角色前端页面全流程设计，覆盖权限管理、档案管理、挂号服务、数据可视化四大核心模块，兼顾业务完整性与毕业设计的可实现性。

## 一、后端数据业务表设计（MySQL）

数据库名：`medical_archive_visual_system`，字符集`utf8mb4`，排序规则`utf8mb4_general_ci`，统一采用**逻辑删除**、创建/更新时间字段，适配MyBatis-Plus开发规范，基于RBAC权限模型设计，覆盖三类核心用户全业务流程。

### （一）系统权限模块（RBAC权限体系）

#### 1. 系统用户表 `sys_user`

核心表，存储管理员、医生、患者三类系统用户的账号信息，实现统一身份认证。

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|user_id|bigint|主键、自增、非空|用户唯一ID|
|username|varchar(50)|非空、唯一|登录账号（工号/手机号）|
|password|varchar(100)|非空|加密存储密码（BCrypt加密）|
|real_name|varchar(20)|非空|用户真实姓名|
|phone|varchar(11)|唯一|手机号|
|id_card|varchar(18)|唯一|身份证号（加密存储）|
|gender|tinyint||性别：1-男 2-女 0-未知|
|role_type|tinyint|非空|角色类型：1-系统管理员 2-医生 3-患者|
|status|tinyint|非空、默认1|账号状态：1-启用 0-禁用|
|avatar|varchar(255)||头像地址|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除：0-未删除 1-已删除|
#### 2. 系统角色表 `sys_role`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|role_id|bigint|主键、自增、非空|角色ID|
|role_name|varchar(30)|非空、唯一|角色名称（系统管理员/医生/患者）|
|role_code|varchar(30)|非空、唯一|角色权限标识（admin/doctor/patient）|
|description|varchar(255)||角色描述|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
#### 3. 系统权限表 `sys_permission`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|perm_id|bigint|主键、自增、非空|权限ID|
|parent_id|bigint|非空、默认0|父权限ID（0为顶级菜单）|
|perm_name|varchar(50)|非空|权限/菜单名称|
|perm_code|varchar(100)|唯一|权限标识（如system:user:add）|
|perm_type|tinyint|非空|权限类型：1-菜单 2-按钮 3-接口|
|path|varchar(255)||前端路由地址|
|icon|varchar(100)||菜单图标|
|sort|int|非空、默认0|排序序号|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
#### 4. 用户-角色关联表 `sys_user_role`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|id|bigint|主键、自增、非空|关联ID|
|user_id|bigint|非空|用户ID（外键关联sys_user.user_id）|
|role_id|bigint|非空|角色ID（外键关联sys_role.role_id）|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
#### 5. 角色-权限关联表 `sys_role_permission`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|id|bigint|主键、自增、非空|关联ID|
|role_id|bigint|非空|角色ID（外键关联sys_role.role_id）|
|perm_id|bigint|非空|权限ID（外键关联sys_permission.perm_id）|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
### （二）医院基础信息模块

#### 1. 科室信息表 `hospital_department`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|dept_id|bigint|主键、自增、非空|科室ID|
|dept_name|varchar(50)|非空、唯一|科室名称（如内科、外科）|
|dept_code|varchar(30)|非空、唯一|科室编码|
|parent_id|bigint|非空、默认0|父科室ID（0为一级科室）|
|description|varchar(255)||科室简介|
|sort|int|非空、默认0|排序序号|
|status|tinyint|非空、默认1|科室状态：1-启用 0-停用|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
#### 2. 医生信息表 `doctor_info`

关联用户表与科室表，存储医生执业相关信息

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|doctor_id|bigint|主键、自增、非空|医生ID|
|user_id|bigint|非空、唯一|关联系统用户ID（外键sys_user.user_id）|
|dept_id|bigint|非空|所属科室ID（外键hospital_department.dept_id）|
|job_title|varchar(30)|非空|职称（主任医师/副主任医师/主治医师/住院医师）|
|specialty|varchar(255)|非空|专业擅长|
|introduction|text||医生简介|
|registration_fee|decimal(10,2)|非空|挂号费用|
|schedule|varchar(255)||出诊时间|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
### （三）患者与医疗档案核心模块

#### 1. 患者基本信息表 `patient_info`

关联用户表，存储患者健康基础档案

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|patient_id|bigint|主键、自增、非空|患者ID|
|user_id|bigint|非空、唯一|关联系统用户ID（外键sys_user.user_id）|
|birth_date|date||出生日期|
|age|int||年龄|
|blood_type|varchar(10)||血型（A/B/O/AB/RH阴/阳）|
|marital_status|tinyint||婚姻状况：1-未婚 2-已婚 3-离异 4-丧偶|
|address|varchar(255)||居住地址|
|emergency_contact|varchar(20)||紧急联系人姓名|
|emergency_phone|varchar(11)||紧急联系人电话|
|allergy_history|text||过敏史|
|past_medical_history|text||既往病史|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
#### 2. 电子病历主表 `medical_record`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|record_id|bigint|主键、自增、非空|病历ID|
|patient_id|bigint|非空|患者ID（外键patient_info.patient_id）|
|doctor_id|bigint|非空|接诊医生ID（外键doctor_info.doctor_id）|
|dept_id|bigint|非空|就诊科室ID（外键hospital_department.dept_id）|
|registration_id|bigint||关联挂号记录ID（外键registration_record.registration_id）|
|visit_date|datetime|非空|就诊时间|
|chief_complaint|text|非空|主诉|
|present_illness|text||现病史|
|past_history|text||既往史|
|physical_examination|text||体格检查|
|auxiliary_examination|text||辅助检查结果|
|diagnosis|varchar(255)|非空|诊断结果|
|treatment_plan|text||治疗方案|
|record_status|tinyint|非空、默认1|病历状态：1-已完成 0-草稿|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
#### 3. 患者健康监测数据表 `health_monitor`

存储患者日常健康指标，用于趋势可视化

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|monitor_id|bigint|主键、自增、非空|监测记录ID|
|patient_id|bigint|非空|患者ID（外键patient_info.patient_id）|
|monitor_date|datetime|非空|监测时间|
|systolic_pressure|int||收缩压（mmHg）|
|diastolic_pressure|int||舒张压（mmHg）|
|blood_glucose|decimal(5,2)||血糖值（mmol/L）|
|heart_rate|int||心率（次/分）|
|body_temperature|decimal(3,1)||体温（℃）|
|weight|decimal(5,2)||体重（kg）|
|remark|varchar(255)||备注|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
### （四）挂号诊疗业务模块

#### 挂号预约记录表 `registration_record`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|registration_id|bigint|主键、自增、非空|挂号记录ID|
|registration_no|varchar(30)|非空、唯一|挂号单号|
|patient_id|bigint|非空|患者ID（外键patient_info.patient_id）|
|doctor_id|bigint|非空|接诊医生ID（外键doctor_info.doctor_id）|
|dept_id|bigint|非空|就诊科室ID（外键hospital_department.dept_id）|
|schedule_date|date|非空|预约就诊日期|
|time_slot|varchar(30)|非空|就诊时段（上午/下午/夜间）|
|registration_fee|decimal(10,2)|非空|挂号费用|
|pay_status|tinyint|非空、默认0|支付状态：0-未支付 1-已支付 2-已退款|
|registration_status|tinyint|非空、默认0|挂号状态：0-待就诊 1-已就诊 2-已取消 3-已过期|
|visit_serial_number|int||就诊序号|
|remark|varchar(255)||备注|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
### （五）数据可视化与聚类分析模块

#### 患者群体聚类结果表 `patient_cluster`

适配开题报告K-means聚类算法创新点，存储患者分群结果，支撑可视化展示

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|cluster_id|bigint|主键、自增、非空|聚类记录ID|
|cluster_version|varchar(30)|非空|聚类版本号（按批次区分）|
|patient_id|bigint|非空|患者ID（外键patient_info.patient_id）|
|cluster_group|int|非空|聚类分群编号（如1/2/3/4类）|
|group_name|varchar(50)|非空|分群标签（如高血压高危人群、糖尿病随访人群）|
|feature_vector|text||聚类特征向量JSON存储|
|cluster_time|datetime|非空|聚类执行时间|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
### （六）系统辅助功能模块

#### 健康资讯表 `system_news`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|news_id|bigint|主键、自增、非空|资讯ID|
|title|varchar(100)|非空|资讯标题|
|author|varchar(30)|非空|发布作者|
|cover_image|varchar(255)||封面图地址|
|content|longtext|非空|资讯正文内容|
|view_count|int|非空、默认0|浏览量|
|is_top|tinyint|非空、默认0|是否置顶：1-置顶 0-不置顶|
|status|tinyint|非空、默认1|状态：1-发布 0-草稿 2-下架|
|publish_time|datetime||发布时间|
|create_time|datetime|非空、默认CURRENT_TIMESTAMP|创建时间|
|update_time|datetime|非空、默认CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP|更新时间|
|is_deleted|tinyint|非空、默认0|逻辑删除|
#### 系统操作日志表 `sys_operation_log`

|字段名|数据类型|约束|字段说明|
|---|---|---|---|
|log_id|bigint|主键、自增、非空|日志ID|
|user_id|bigint||操作用户ID|
|username|varchar(50)||操作用户账号|
|operation_module|varchar(50)|非空|操作模块（如用户管理、挂号管理）|
|operation_type|varchar(30)|非空|操作类型（新增/删除/修改/查询/登录）|
|operation_content|varchar(500)||操作内容|
|request_method|varchar(10)||请求方式（GET/POST/PUT/DELETE）|
|request_url|varchar(255)||请求地址|
|ip_address|varchar(50)||操作IP地址|
|operation_status|tinyint|非空|操作状态：1-成功 0-失败|
|error_msg|text||异常信息|
|operation_time|datetime|非空|操作时间|
|cost_time|bigint||接口耗时（毫秒）|
## 二、前端页面设计

### （一）核心技术选型与整体架构

1. **核心技术栈**：Vue 3 + Vite + Element Plus UI组件库 + Vue Router（路由管理） + Pinia（状态管理） + Axios（接口请求） + ECharts 5（数据可视化）

2. **整体架构**：采用单页面应用（SPA）前后端分离模式，基于RBAC权限模型实现**菜单动态渲染**与**按钮级权限控制**，分为公共页面、管理员端、医生端、患者端四大模块，所有页面适配PC端响应式布局，核心接口请求统一封装，加入请求/响应拦截器实现JWT令牌自动携带与异常统一处理。

3. **路由设计核心规则**：

    - 公共路由：登录、注册、忘记密码，无需权限即可访问

    - 权限路由：根据登录用户的`role_type`动态加载对应角色的路由菜单，实现三端权限隔离

    - 路由守卫：全局前置守卫校验用户登录状态与权限，未登录用户强制跳转至登录页，无权限访问的路由自动拦截

### （二）公共页面设计

1. **登录页**

    - 核心功能：账号密码登录、手机号验证码登录、角色选择（管理员/医生/患者）、记住密码、忘记密码跳转、注册页跳转

    - 页面布局：左侧品牌与系统介绍区，右侧登录表单区，加入表单校验（账号非空、密码长度校验）、登录加载状态、错误提示弹窗

2. **注册页**

    - 核心功能：患者账号注册（医生/管理员账号由后台统一创建）、手机号验证码校验、密码二次确认、身份证号实名校验、用户协议勾选

    - 页面布局：分步式注册表单，加入表单实时校验、验证码倒计时、注册成功跳转登录页

3. **忘记密码页**

    - 核心功能：手机号验证码身份校验、新密码设置、密码强度校验、提交后跳转登录页

### （三）系统管理员端页面设计

整体布局：左侧固定导航菜单、顶部面包屑+用户信息+退出登录、右侧主内容区，核心围绕**系统管理+全院运营数据可视化**设计。

|页面名称|核心功能与布局设计|可视化组件设计|
|---|---|---|
|运营数据大盘（首页）|顶部核心指标卡片：全院累计挂号量、今日就诊人次、活跃患者数、科室总数、医生总数；<br>中部多维度数据统计区；<br>底部患者群体分析区|1. 挂号量趋势折线图：近12个月全院挂号量走势，支持按科室筛选；<br>2. 科室挂号占比饼图：各科室挂号量分布，支持钻取查看科室详情；<br>3. 疾病TOP10柱状图：全院就诊疾病排名，支持按时间范围筛选；<br>4. 患者年龄分布雷达图/柱状图：就诊患者年龄分层统计；<br>5. 患者群体聚类散点图：基于K-means算法的患者分群可视化，标注各群体核心特征|
|用户管理页|表格布局，支持分页、模糊搜索、按角色/状态筛选；<br>核心操作：用户新增、编辑、启用/禁用、重置密码、角色分配、批量操作|无核心可视化组件，辅助加入用户角色分布饼图|
|科室管理页|树形表格展示科室层级，支持分页、搜索；<br>核心操作：科室新增/编辑/删除、排序、启用/停用|无核心可视化组件|
|医生管理页|表格布局，关联科室筛选，支持分页、搜索；<br>核心操作：医生信息新增/编辑/删除、关联科室、出诊时间设置、挂号费配置|辅助加入各科室医生数量分布柱状图|
|挂号管理页|全量挂号记录表格，支持按科室/医生/挂号状态/时间范围多维度筛选；<br>核心操作：挂号记录详情查看、状态修改、数据导出、退款操作|辅助加入挂号状态分布饼图、每日挂号量趋势图|
|病历档案管理页|全量病历记录表格，支持按患者/医生/科室/就诊时间筛选；<br>核心操作：病历详情查看、审核、打印、数据导出|辅助加入疾病类型分布统计图表|
|资讯管理页|资讯列表表格，支持按状态/发布时间筛选；<br>核心操作：富文本编辑器新增/编辑资讯、发布/下架、置顶、浏览量统计|无核心可视化组件|
|系统管理页|包含角色管理、权限管理、菜单管理、操作日志四个子页面；<br>核心功能：角色增删改查、权限分配、菜单配置、操作日志查询与导出|无核心可视化组件|
### （四）医生端页面设计

整体布局：左侧导航菜单、顶部工作台信息+个人中心、右侧主内容区，核心围绕**诊疗服务+患者临床数据可视化**设计。

|页面名称|核心功能与布局设计|可视化组件设计|
|---|---|---|
|诊疗工作台（首页）|顶部核心指标卡片：今日预约数、待就诊人数、累计接诊量、本月接诊排名；<br>中部待就诊患者列表；<br>底部历史诊疗数据统计区|1. 个人接诊量趋势折线图：近6个月接诊量走势；<br>2. 接诊疾病分布饼图：个人诊疗疾病类型占比；<br>3. 科室接诊排名柱状图：同科室医生接诊量对比|
|我的预约管理页|分标签页展示：待就诊、已就诊、已取消预约列表；<br>核心操作：接诊确认、患者详情跳转、病历新增、预约取消|辅助加入今日预约时段分布柱状图|
|患者档案管理页|左侧接诊患者列表，支持搜索筛选；<br>右侧患者档案详情，分标签页展示：基本信息、历史病历、健康监测数据、诊疗记录|1. 患者健康指标趋势折线图：血压、血糖、心率等指标的时间维度变化趋势，支持多指标对比；<br>2. 患者就诊频次柱状图：年度/月度就诊次数统计；<br>3. 同类患者群体对比雷达图：基于K-means聚类结果，展示当前患者与同群体患者的健康指标对比|
|电子病历管理页|病历列表，支持按就诊时间/诊断结果筛选；<br>核心功能：病历新增（富文本表单）、编辑、查看、打印、处方开具|无核心可视化组件|
|诊疗数据统计页|多维度个人诊疗数据统计，支持按时间范围筛选|1. 诊疗效果统计图表；<br>2. 患者复诊率折线图；<br>3. 单病种诊疗时长分布统计|
### （五）患者端页面设计

整体布局：顶部导航栏、左侧快捷菜单、右侧主内容区，轻量化设计，核心围绕**个人健康管理+就诊服务+个人健康数据可视化**设计，降低操作门槛。

|页面名称|核心功能与布局设计|可视化组件设计|
|---|---|---|
|健康首页|顶部个人健康概览卡片；<br>中部快捷功能入口（在线挂号、我的档案、健康监测）；<br>底部最近就诊记录+健康资讯推荐|1. 个人健康指标概览仪表盘：展示血压、血糖等核心指标是否在正常范围；<br>2. 就诊记录时间轴可视化|
|在线挂号页|分步式挂号流程：第一步选择科室，第二步选择医生（展示医生职称、擅长、出诊时间、挂号费），第三步选择就诊日期与时段，第四步确认支付；<br>配套挂号记录查询入口|无核心可视化组件|
|我的挂号记录页|分标签页展示：待就诊、已就诊、已取消、已退款挂号记录；<br>核心操作：挂号详情查看、取消预约、支付、就诊导航|辅助加入年度挂号科室分布饼图|
|个人医疗档案页|分标签页展示：个人基本信息、历史病历、就诊记录；<br>核心功能：基本信息维护、病历详情查看、病历打印、就诊记录导出|1. 个人就诊频次折线图：近12个月就诊次数走势；<br>2. 就诊疾病分布饼图：历史就诊疾病类型占比|
|健康监测管理页|顶部健康数据录入表单；<br>中部历史监测数据列表；<br>底部数据趋势分析区|1. 健康指标多维度趋势折线图：血压、血糖、体重等指标的时间维度变化，支持按周/月/年切换；<br>2. 指标正常范围对比可视化，异常值高亮标注|
|个人中心页|账号信息维护、密码修改、消息通知、隐私设置、退出登录|无核心可视化组件|
### （六）核心交互与兼容性设计

1. **权限交互**：用户登录后，根据角色类型动态渲染对应菜单，无权限的路由与按钮自动隐藏，接口请求加入权限校验，防止越权访问。

2. **可视化交互**：所有ECharts图表支持**hover提示框、图例筛选、时间范围切换、数据钻取、图表导出（PNG/PDF）、全屏查看**功能，适配不同屏幕尺寸的自适应渲染。

3. **表单交互**：所有表单加入实时校验、输入提示、操作反馈、防重复提交功能，复杂表单采用分步式设计，降低用户操作成本。

4. **兼容性**：适配Chrome、Edge、Firefox等主流浏览器，最小适配分辨率1366*768，保证在医院办公电脑的常规分辨率下正常展示与操作。
> （注：文档部分内容可能由 AI 生成）