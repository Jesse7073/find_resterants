-- 初始公開標籤種子資料，由開發者維護（見 docs/phase2-requirements-spec.md 4.4）
-- 之後要新增/調整公開標籤，一律新增 V3、V4... migration script，不修改本檔

INSERT INTO public_tags (name) VALUES
    ('辣'),
    ('適合約會'),
    ('適合家庭聚餐'),
    ('適合一人食'),
    ('平價'),
    ('高級餐廳'),
    ('可預約'),
    ('寵物友善');
