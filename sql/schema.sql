create table claim_record
(
    id                  bigint auto_increment comment '申请主键'
        primary key,
    item_id             bigint                             not null comment '申请的物品ID',
    publisher_id        bigint                             not null comment '发帖人ID',
    applicant_id        bigint                             not null comment '申请人ID',
    answer              varchar(255)                       not null comment '申请者填写的暗号答案',
    status              tinyint  default 0                 not null comment '0:待审核, 1:同意, 2:拒绝, 3:补充证据 4:补充已提交(等待最终审核)',
    create_time         datetime default CURRENT_TIMESTAMP null comment '申请时间',
    update_time         datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '最后修改时间',
    supplement_question varchar(500)                       null comment '发帖人给这个申请设置的补充核验问题',
    supplement_answer   varchar(500)                       null comment '认领者的补充回答'
)
    comment '认领申请记录表';

create index claim_record_publisher_id
    on claim_record (publisher_id);

create index idx_applicant_id
    on claim_record (applicant_id);

create index idx_item_id
    on claim_record (item_id);

create table item_comment
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    item_id        bigint                               not null comment '所属帖子ID',
    user_id        bigint                               not null comment '评论发布者ID',
    target_user_id bigint                               not null comment '帖主ID (被通知人)',
    content        varchar(512)                         not null comment '留言内容',
    is_read        tinyint(1) default 0                 null comment '帖主是否已读 (0:未读, 1:已读)',
    create_time    datetime   default CURRENT_TIMESTAMP null comment '留言时间'
)
    comment '失物信息留言表';

create table item_detail
(
    item_id           bigint not null comment '关联主表ID'
        primary key,
    semi_public_desc  text   null comment '半公开细节（注册满24小时可见）',
    images_url        text   null comment '图片URL数组（JSON字符串形式）',
    ai_generated_desc text   null comment 'AI辅助生成的描述备份'
)
    comment '物品详情表（1对1）';

create table item_info
(
    id                bigint auto_increment comment '物品主键'
        primary key,
    user_id           bigint                             not null comment '发布者用户ID',
    type              tinyint                            not null comment '0:丢失，1:拾取',
    item_name         varchar(100)                       not null comment '物品标题',
    event_time        datetime                           not null comment '丢失/拾取发生时间',
    location          varchar(255)                       not null comment '发生地点',
    public_desc       varchar(500)                       not null comment '公开可见的简述',
    ai_category       varchar(50)                        null comment 'ai生成的分类',
    status            tinyint  default 0                 not null comment '0:寻找中，1:锁定中，2:已结案',
    is_top            tinyint  default 0                 not null comment '是否置顶（0:否，1:是）',
    top_end_time      datetime                           null comment '置顶结束时间',
    is_deleted        tinyint  default 0                 not null comment '是否被逻辑删除（0：否，1是）',
    create_time       datetime default CURRENT_TIMESTAMP null,
    update_time       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    latest_reply_time datetime default CURRENT_TIMESTAMP null comment '最后被回复时间（拿来做排序）'
)
    comment '物品核心主表';

create index idx_type_status
    on item_info (type, status);

create index idx_user_id
    on item_info (user_id);

create table item_secure
(
    item_id         bigint       not null comment '关联主表ID'
        primary key,
    verify_question varchar(255) null comment '核验问题',
    verify_answer   varchar(255) null comment '核验暗号',
    private_contact varchar(255) null comment '拾取者的真实联系方式'
)
    comment '物品安全核验表';

create table private_message
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    sender_id   bigint                               not null comment '发送方ID',
    receiver_id bigint                               not null comment '接收方ID',
    content     varchar(1024)                        not null comment '私信内容',
    is_read     tinyint(1) default 0                 null comment '接收方是否已读 (0:未读, 1:已读)',
    create_time datetime   default CURRENT_TIMESTAMP null comment '发送时间'
)
    comment '私信表';

create table report_record
(
    id             bigint auto_increment comment '举报主键'
        primary key,
    item_id        bigint                             not null comment '被举报的物品ID',
    reporter_id    bigint                             not null comment '举报人ID',
    reason         varchar(255)                       not null comment '举报理由',
    status         tinyint  default 0                 not null comment '0:待处理, 1:核实, 2:驳回',
    create_time    datetime default CURRENT_TIMESTAMP null,
    process_remark varchar(255)                       null comment '管理员处理备注'
)
    comment '举报管理表';

create index idx_item_id
    on report_record (item_id);

create table top_apply_record
(
    id             bigint auto_increment comment '申请主键'
        primary key,
    item_id        bigint                             not null comment '申请置顶的物品ID',
    user_id        bigint                             not null comment '申请人ID',
    apply_reason   varchar(255)                       not null comment '申请理由',
    status         tinyint  default 0                 not null comment '0:待审核, 1:已同意, 2:已拒绝',
    create_time    datetime default CURRENT_TIMESTAMP null comment '申请时间',
    process_remark varchar(255)                       null comment '管理员处理备注'
)
    comment '置顶申请记录表';

create table user
(
    id               bigint auto_increment comment '主键'
        primary key,
    username         varchar(50)                           not null comment '用户名，全局唯一',
    nickname         varchar(50) default '新用户'          not null comment '展示昵称',
    email            varchar(100)                          not null comment '登录凭证A',
    phone            varchar(20)                           not null comment '登录凭证B',
    password         varchar(255)                          not null comment '密码，非明文存储',
    avatar_url       varchar(255)                          null comment '用户头像链接',
    role             tinyint                               not null comment '0:学生, 1:管理员',
    status           tinyint     default 0                 not null comment '0:正常, 1:封禁',
    last_active_time datetime                              null comment '最后活跃时间',
    create_time      datetime    default CURRENT_TIMESTAMP not null comment '注册时间',
    update_time      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后修改时间',
    is_deleted       tinyint     default 0                 not null comment '逻辑删除（0:未删, 1:已删）',
    constraint uk_email
        unique (email),
    constraint uk_phone
        unique (phone),
    constraint uk_username
        unique (username)
)
    comment '用户核心表';

create table user_active_log
(
    id          bigint auto_increment comment '流水主键'
        primary key,
    user_id     bigint   not null comment '活跃用户ID',
    role        tinyint  not null,
    active_time datetime not null comment '具体活跃时间点（B+树索引）'
)
    comment '用户活跃流水表';

create index idx_time
    on user_active_log (active_time);

create index idx_user_id
    on user_active_log (user_id);

create table users
(
    id            bigint unsigned auto_increment
        primary key,
    display_name  varchar(64)                                   not null,
    email         varchar(191)                                  null,
    password_hash varchar(255)                                  null,
    status        tinyint unsigned default '1'                  not null,
    created_at    datetime(3)      default CURRENT_TIMESTAMP(3) not null,
    updated_at    datetime(3)      default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3),
    constraint uk_users_email
        unique (email)
);

create table diaries
(
    id              bigint unsigned auto_increment
        primary key,
    author_user_id  bigint unsigned                                         not null,
    author_name     varchar(64)                                             not null,
    content         text                                                    not null,
    visibility      enum ('PRIVATE', 'PUBLIC') default 'PRIVATE'            not null,
    resonance_count int unsigned               default '0'                  not null,
    is_deleted      tinyint(1)                 default 0                    not null,
    created_at      datetime(3)                default CURRENT_TIMESTAMP(3) not null,
    updated_at      datetime(3)                default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3),
    constraint fk_diaries_author_user
        foreign key (author_user_id) references users (id)
);

create index idx_diaries_author_created
    on diaries (author_user_id, created_at);

create index idx_diaries_visibility_created
    on diaries (visibility, created_at);

create table diary_analysis
(
    diary_id          bigint unsigned                          not null
        primary key,
    mood_label        varchar(32)                              not null,
    mood_intensity    tinyint unsigned                         not null,
    topic_labels_json json                                     not null,
    summary           varchar(255)                             not null,
    feedback          varchar(500)                             not null,
    created_at        datetime(3) default CURRENT_TIMESTAMP(3) not null,
    updated_at        datetime(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3),
    constraint fk_diary_analysis_diary
        foreign key (diary_id) references diaries (id)
            on delete cascade,
    constraint chk_diary_analysis_intensity
        check (`mood_intensity` between 1 and 5)
);

create table diary_comments
(
    id             bigint unsigned auto_increment
        primary key,
    diary_id       bigint unsigned                          not null,
    author_user_id bigint unsigned                          null,
    author_name    varchar(64)                              not null,
    content        varchar(1000)                            not null,
    is_deleted     tinyint(1)  default 0                    not null,
    created_at     datetime(3) default CURRENT_TIMESTAMP(3) not null,
    updated_at     datetime(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP(3),
    constraint fk_diary_comments_author_user
        foreign key (author_user_id) references users (id)
            on delete set null,
    constraint fk_diary_comments_diary
        foreign key (diary_id) references diaries (id)
            on delete cascade
);

create index idx_diary_comments_author_created
    on diary_comments (author_user_id, created_at);

create index idx_diary_comments_diary_created
    on diary_comments (diary_id, created_at);

create table diary_resonances
(
    id         bigint unsigned auto_increment
        primary key,
    diary_id   bigint unsigned                          not null,
    user_id    bigint unsigned                          not null,
    created_at datetime(3) default CURRENT_TIMESTAMP(3) not null,
    constraint uk_diary_resonances_diary_user
        unique (diary_id, user_id),
    constraint fk_diary_resonances_diary
        foreign key (diary_id) references diaries (id)
            on delete cascade,
    constraint fk_diary_resonances_user
        foreign key (user_id) references users (id)
            on delete cascade
);

create index idx_diary_resonances_user_created
    on diary_resonances (user_id, created_at);

create table notifications
(
    id                bigint unsigned auto_increment
        primary key,
    recipient_user_id bigint unsigned                                  not null,
    actor_user_id     bigint unsigned                                  null,
    diary_id          bigint unsigned                                  null,
    comment_id        bigint unsigned                                  null,
    type              enum ('COMMENT', 'REPLY', 'RESONANCE', 'SYSTEM') not null,
    message           varchar(255)                                     not null,
    is_read           tinyint(1)  default 0                            not null,
    read_at           datetime(3)                                      null,
    created_at        datetime(3) default CURRENT_TIMESTAMP(3)         not null,
    constraint fk_notifications_actor_user
        foreign key (actor_user_id) references users (id)
            on delete set null,
    constraint fk_notifications_comment
        foreign key (comment_id) references diary_comments (id)
            on delete cascade,
    constraint fk_notifications_diary
        foreign key (diary_id) references diaries (id)
            on delete cascade,
    constraint fk_notifications_recipient_user
        foreign key (recipient_user_id) references users (id)
            on delete cascade
);

create index idx_notifications_diary
    on notifications (diary_id);

create index idx_notifications_recipient_read_created
    on notifications (recipient_user_id, is_read, created_at);

create index idx_users_created_at
    on users (created_at);
