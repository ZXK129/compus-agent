-- ===================================================
-- 星空大学校园智能体终端 - 数据库初始化脚本
-- Database: campus_db
-- 执行方式: mysql -u root -p < init.sql
-- ===================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_db;

-- ===================================================
-- 1. 学生表
-- ===================================================
DROP TABLE IF EXISTS tb_chat_message;
DROP TABLE IF EXISTS tb_seat;
DROP TABLE IF EXISTS tb_campus_moment;
DROP TABLE IF EXISTS tb_library_book;
DROP TABLE IF EXISTS tb_transaction;
DROP TABLE IF EXISTS tb_card;
DROP TABLE IF EXISTS tb_course;
DROP TABLE IF EXISTS tb_student;

CREATE TABLE tb_student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_no VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    department VARCHAR(100) NOT NULL COMMENT '院系',
    gpa DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT 'GPA绩点',
    credits_earned INT NOT NULL DEFAULT 0 COMMENT '已修学分',
    credits_required INT NOT NULL DEFAULT 150 COMMENT '毕业学分要求',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ===================================================
-- 2. 课程表
-- ===================================================
CREATE TABLE tb_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '课程名称',
    code VARCHAR(20) NOT NULL COMMENT '课程编号',
    instructor VARCHAR(100) NOT NULL COMMENT '授课教师',
    course_time VARCHAR(50) NOT NULL COMMENT '上课时间段 (如: 08:30 - 10:00)',
    weekday TINYINT NOT NULL COMMENT '周几 (1-Mon 2-Tue 3-Wed 4-Thu 5-Fri)',
    location VARCHAR(200) NOT NULL COMMENT '上课地点',
    credits INT NOT NULL DEFAULT 2 COMMENT '学分',
    category VARCHAR(20) NOT NULL COMMENT '课程类别: Major/Elective/General',
    color VARCHAR(30) NOT NULL DEFAULT 'bg-neutral-900' COMMENT '前端展示颜色',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ===================================================
-- 3. 一卡通表
-- ===================================================
CREATE TABLE tb_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '关联学生ID',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '卡余额',
    card_no VARCHAR(30) NOT NULL UNIQUE COMMENT '卡号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES tb_student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一卡通表';

-- ===================================================
-- 4. 消费/充值记录表
-- ===================================================
CREATE TABLE tb_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL COMMENT '关联一卡通ID',
    item VARCHAR(200) NOT NULL COMMENT '事项描述',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    type VARCHAR(10) NOT NULL COMMENT '类型: expense/income',
    category VARCHAR(20) NOT NULL COMMENT '分类: canteen/transit/printing/sports/utility',
    location VARCHAR(200) DEFAULT '' COMMENT '地点',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    FOREIGN KEY (card_id) REFERENCES tb_card(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费/充值记录表';

-- ===================================================
-- 5. 图书馆借阅表
-- ===================================================
CREATE TABLE tb_library_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '借阅学生ID',
    title VARCHAR(300) NOT NULL COMMENT '书名',
    author VARCHAR(200) NOT NULL COMMENT '作者',
    isbn VARCHAR(20) NOT NULL COMMENT 'ISBN',
    cover_color VARCHAR(30) NOT NULL DEFAULT 'bg-neutral-900' COMMENT '前端展示颜色',
    due_date DATE NOT NULL COMMENT '归还截止日期',
    progress INT NOT NULL DEFAULT 0 COMMENT '阅读进度 0-100',
    renewed TINYINT NOT NULL DEFAULT 0 COMMENT '是否已续借 0-未续 1-已续',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES tb_student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书馆借阅表';

-- ===================================================
-- 6. 校园动态表
-- ===================================================
CREATE TABLE tb_campus_moment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '发布学生ID',
    title VARCHAR(500) NOT NULL COMMENT '动态内容',
    tag VARCHAR(50) NOT NULL COMMENT '标签分类',
    location VARCHAR(200) NOT NULL COMMENT '位置',
    likes INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    joined TINYINT NOT NULL DEFAULT 0 COMMENT '当前用户是否已报名',
    max_attendees INT DEFAULT NULL COMMENT '最大参与人数',
    current_attendees INT DEFAULT 0 COMMENT '当前参与人数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    FOREIGN KEY (student_id) REFERENCES tb_student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园动态表';

-- ===================================================
-- 7. 自习座位表
-- ===================================================
CREATE TABLE tb_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_code VARCHAR(20) NOT NULL UNIQUE COMMENT '座位编号',
    floor_area VARCHAR(20) NOT NULL COMMENT '楼层区域',
    status VARCHAR(20) NOT NULL DEFAULT 'available' COMMENT '状态: available/occupied',
    student_id BIGINT DEFAULT NULL COMMENT '占用学生ID',
    booked_start DATETIME DEFAULT NULL COMMENT '预约开始时间',
    booked_end DATETIME DEFAULT NULL COMMENT '预约结束时间',
    FOREIGN KEY (student_id) REFERENCES tb_student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习座位表';

-- ===================================================
-- 8. AI 对话记录表 (可选)
-- ===================================================
CREATE TABLE tb_chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    role VARCHAR(10) NOT NULL COMMENT '角色: user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES tb_student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';

-- ===================================================
-- ==================== 示例数据 ======================
-- ===================================================

-- 示例学生
INSERT INTO tb_student (id, student_no, name, department, gpa, credits_earned, credits_required) VALUES
(1, '202611042', '赵肖凯', '理学部', 3.85, 98, 150);

-- 示例课程 (对应原项目数据)
INSERT INTO tb_course (name, code, instructor, course_time, weekday, location, credits, category, color) VALUES
('人工智慧与脑机接口',      'CS-402', '林清玄 教授', '08:30 - 10:00', 1, '新星科技楼 402',      3, 'Major',    'bg-neutral-900'),
('经典中国哲学导读',        'PH-105', '罗翔 教授',   '14:00 - 15:30', 1, '综合报告厅',            2, 'General',  'bg-neutral-600'),
('算法设计与数理逻辑',      'CS-301', '高德纳 教授', '10:15 - 11:45', 2, '理学实验楼 B3',         4, 'Major',    'bg-neutral-900'),
('计算社会科学与数据思维',  'SS-203', '戴安娜 博士', '16:00 - 17:30', 2, '社科人文讲堂',          3, 'Elective', 'bg-neutral-350'),
('机器学习与工程实践应用',  'CS-411', '杨立昆 教授', '08:30 - 10:00', 3, 'AI重点实验室 101',      3, 'Major',    'bg-neutral-900'),
('交互设计与未来界面',      'DS-102', '艾维 教授',   '14:00 - 15:30', 3, '创意设计大楼 2A',       2, 'Elective', 'bg-neutral-350'),
('数字孪生校园建构与算力',  'CS-499', '张朝阳 博士', '10:15 - 11:45', 4, '算力指挥大楼 302',      3, 'Major',    'bg-neutral-900'),
('智能体工程实践(Agent)',   'CS-455', '奥特曼 教授', '08:30 - 10:00', 5, '硅谷产业研究孵化室',    3, 'Major',    'bg-neutral-900');

-- 示例一卡通
INSERT INTO tb_card (id, student_id, balance, card_no) VALUES
(1, 1, 138.50, 'STAR-2026-11042');

-- 示例消费记录
INSERT INTO tb_transaction (card_id, item, amount, type, category, location, created_at) VALUES
(1, '清真餐厅特色牛肉拉面',       15.00, 'expense', 'canteen',  '第一食堂一楼',           '2026-06-23 12:15:00'),
(1, '星空巴士单次特惠扣费',        2.00, 'expense', 'transit',  '星空南门巴士站',         '2026-06-23 08:12:00'),
(1, '数字图书馆激光自助高精打印',  3.50, 'expense', 'printing', '图书馆四楼自助打印终端', '2026-06-22 16:30:00');

-- 示例借阅图书
INSERT INTO tb_library_book (id, student_id, title, author, isbn, cover_color, due_date, progress, renewed) VALUES
(1, 1, 'The Pragmatic Programmer',  'Andrew Hunt & David Thomas', '9780135957059', 'bg-neutral-900', '2026-06-27', 68, 0),
(2, 1, 'Introduction to Algorithms', 'Thomas H. Cormen',          '9780262033848', 'bg-neutral-600', '2026-07-04', 24, 0);

-- 示例校园动态
INSERT INTO tb_campus_moment (id, student_id, title, tag, location, likes, joined, max_attendees, current_attendees, created_at) VALUES
(1, 1, '下午在大剧场排练乐团《星际穿越》管弦，现场有免费的香草拿铁与曲奇，欢迎乐器小伙伴加入！🎸', '组队敲码', '艺术中心排演大厅', 24, 0, NULL, 0,   '2026-06-23 13:48:00'),
(2, 1, '星空AI实验室暑期科技创新营招新考核大纲发布啦！有意向的本科生可来4层申领自备研讨空间。',     '学术讨论', '理学实验中心',     89, 0, 30, 21,  '2026-06-23 11:00:00'),
(3, 1, '星空大操场「草坪音乐节活动」今日 19:30 正常开演，求占座组队！🍉',                         '活动约件', '星空中央大草坪',    112, 0, 150, 94, '2026-06-23 09:00:00');

-- 示例自习座位（1F-3F，每层256座，启动时由 DataInitializer 自动生成完整数据）
-- 此处仅插入少量示例，完整 768 座由 LibraryDataInitializer 在首次启动时批量生成
INSERT INTO tb_seat (id, seat_code, floor_area, status, student_id, booked_start, booked_end) VALUES
(1,  '1F001', '1F', 'available', NULL, NULL, NULL),
(2,  '1F002', '1F', 'available', NULL, NULL, NULL),
(3,  '1F003', '1F', 'occupied',  1,    '2026-06-23 14:00:00', '2026-06-23 18:00:00'),
(4,  '2F001', '2F', 'available', NULL, NULL, NULL),
(5,  '2F002', '2F', 'available', NULL, NULL, NULL),
(6,  '3F001', '3F', 'available', NULL, NULL, NULL),
(7,  '3F002', '3F', 'available', NULL, NULL, NULL),
(8,  '3F003', '3F', 'available', NULL, NULL, NULL);
