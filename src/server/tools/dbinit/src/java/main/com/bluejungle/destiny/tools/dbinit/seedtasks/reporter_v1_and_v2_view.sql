
CREATE VIEW APP_USER_VIEW AS
SELECT username, first_name, last_name, DOMAIN_ID, PRIMARY_GROUP_ID, password, INITLOGIN_DONE FROM SUPER_APPLICATION_USER
UNION ALL
SELECT username, first_name, last_name, DOMAIN_ID, PRIMARY_GROUP_ID, password, INITLOGIN_DONE FROM APPLICATION_USER
  WHERE STATUS != 'DELETED' AND NOT EXISTS (
  	SELECT username FROM SUPER_APPLICATION_USER WHERE APPLICATION_USER.username = SUPER_APPLICATION_USER.username
  );

CREATE VIEW policy_log_v1 AS
  SELECT 
    id as id,
    time as timestamp,
    month_nb as month,
    day_nb as day,
    host_name as host_name,
    user_name as user_name,
    user_id as user_sid,
    application_name as application_name,
    action as action,
    policy_fullname as policy_full_name,
    policy_decision as policy_decision,
    log_level as log_level,
    from_resource_name as source_resource_name,
    from_resource_prefix as source_resource_prefix,
    from_resource_path as source_resource_path,
    from_resource_short_name as source_resource_short_name,
    from_resource_owner_id as source_resource_owner_id,
    from_resource_created_date as source_resource_create_date,
    to_resource_name as target_resource_name
  FROM rpa_log;

CREATE VIEW policy_custom_attribute_v1 AS
SELECT 0 AS id, policy_log_id, attribute_type, attribute_name, attribute_value  FROM
(SELECT
temp.id AS policy_log_id,
temp.attr_type AS attribute_type,
map.name AS attribute_name,
temp.attribute_value
FROM
(SELECT
   Y.id,
   X.attr_type,
   X.mapped_column,
    CASE X.mapped_column
	WHEN 'attr1' THEN Y.attr1
	WHEN 'attr2' THEN Y.attr2
	WHEN 'attr3' THEN Y.attr3
	WHEN 'attr4' THEN Y.attr4
	WHEN 'attr5' THEN Y.attr5
	WHEN 'attr6' THEN Y.attr6
	WHEN 'attr7' THEN Y.attr7
	WHEN 'attr8' THEN Y.attr8
	WHEN 'attr9' THEN Y.attr9
	WHEN 'attr10' THEN Y.attr10
	WHEN 'attr11' THEN Y.attr11
	WHEN 'attr12' THEN Y.attr12
	WHEN 'attr13' THEN Y.attr13
	WHEN 'attr14' THEN Y.attr14
	WHEN 'attr15' THEN Y.attr15
	WHEN 'attr16' THEN Y.attr16
	WHEN 'attr17' THEN Y.attr17
	WHEN 'attr18' THEN Y.attr18
	WHEN 'attr19' THEN Y.attr19
	WHEN 'attr20' THEN Y.attr20
	WHEN 'attr21' THEN Y.attr21
	WHEN 'attr22' THEN Y.attr22
	WHEN 'attr23' THEN Y.attr23
	WHEN 'attr24' THEN Y.attr24
	WHEN 'attr25' THEN Y.attr25
	WHEN 'attr26' THEN Y.attr26
	WHEN 'attr27' THEN Y.attr27
	WHEN 'attr28' THEN Y.attr28
	WHEN 'attr29' THEN Y.attr29
	WHEN 'attr30' THEN Y.attr30
	WHEN 'attr31' THEN Y.attr31
	WHEN 'attr32' THEN Y.attr32
	WHEN 'attr33' THEN Y.attr33
	WHEN 'attr34' THEN Y.attr34
	WHEN 'attr35' THEN Y.attr35
	WHEN 'attr36' THEN Y.attr36
	WHEN 'attr37' THEN Y.attr37
	WHEN 'attr38' THEN Y.attr38
	WHEN 'attr39' THEN Y.attr39
	WHEN 'attr40' THEN Y.attr40
	WHEN 'attr41' THEN Y.attr41
	WHEN 'attr42' THEN Y.attr42
	WHEN 'attr43' THEN Y.attr43
	WHEN 'attr44' THEN Y.attr44
	WHEN 'attr45' THEN Y.attr45
	WHEN 'attr46' THEN Y.attr46
	WHEN 'attr47' THEN Y.attr47
	WHEN 'attr48' THEN Y.attr48
	WHEN 'attr49' THEN Y.attr49
	WHEN 'attr50' THEN Y.attr50
	WHEN 'attr51' THEN Y.attr51
	WHEN 'attr52' THEN Y.attr52
	WHEN 'attr53' THEN Y.attr53
	WHEN 'attr54' THEN Y.attr54
	WHEN 'attr55' THEN Y.attr55
	WHEN 'attr56' THEN Y.attr56
	WHEN 'attr57' THEN Y.attr57
	WHEN 'attr58' THEN Y.attr58
	WHEN 'attr59' THEN Y.attr59
	WHEN 'attr60' THEN Y.attr60
	WHEN 'attr61' THEN Y.attr61
	WHEN 'attr62' THEN Y.attr62
	WHEN 'attr63' THEN Y.attr63
	WHEN 'attr64' THEN Y.attr64
	WHEN 'attr65' THEN Y.attr65
	WHEN 'attr66' THEN Y.attr66
	WHEN 'attr67' THEN Y.attr67
	WHEN 'attr68' THEN Y.attr68
	WHEN 'attr69' THEN Y.attr69
	WHEN 'attr70' THEN Y.attr70
	WHEN 'attr71' THEN Y.attr71
	WHEN 'attr72' THEN Y.attr72
	WHEN 'attr73' THEN Y.attr73
	WHEN 'attr74' THEN Y.attr74
	WHEN 'attr75' THEN Y.attr75
	WHEN 'attr76' THEN Y.attr76
	WHEN 'attr77' THEN Y.attr77
	WHEN 'attr78' THEN Y.attr78
	WHEN 'attr79' THEN Y.attr79
	WHEN 'attr80' THEN Y.attr80
	WHEN 'attr81' THEN Y.attr81
	WHEN 'attr82' THEN Y.attr82
	WHEN 'attr83' THEN Y.attr83
	WHEN 'attr84' THEN Y.attr84
	WHEN 'attr85' THEN Y.attr85
	WHEN 'attr86' THEN Y.attr86
	WHEN 'attr87' THEN Y.attr87
	WHEN 'attr88' THEN Y.attr88
	WHEN 'attr89' THEN Y.attr89
	WHEN 'attr90' THEN Y.attr90
	WHEN 'attr91' THEN Y.attr91
	WHEN 'attr92' THEN Y.attr92
	WHEN 'attr93' THEN Y.attr93
	WHEN 'attr94' THEN Y.attr94
	WHEN 'attr95' THEN Y.attr95
	WHEN 'attr96' THEN Y.attr96
	WHEN 'attr97' THEN Y.attr97
	WHEN 'attr98' THEN Y.attr98
	WHEN 'attr99' THEN Y.attr99
	END AS attribute_value
FROM
   rpa_log  Y CROSS JOIN  (select mapped_column, attr_type FROM rpa_log_mapping where mapped_column is NOT NULL) X) temp
JOIN rpa_log_mapping  map on temp.mapped_column = map.mapped_column
WHERE temp.attribute_value IS NOT NULL

UNION ALL

SELECT policy_log_id, map.name AS attribute_name, attr_value AS attribute_value, attr_type as attribute_type from rpa_log_attr rla
JOIN rpa_log_mapping map on rla.attr_id = map.id)  results;



CREATE VIEW policy_obligation_log_v1 AS
  SELECT 
    id as id,
    ref_log_id as policy_log_id,
    name as name,
    attr_one as attribute_one,
    attr_two as attribute_two,
    attr_three as attribute_three
  FROM report_obligation_log;

CREATE VIEW tracking_log_v1 AS
  SELECT 
    id as id,
    time as timestamp,
    month_nb as month,
    day_nb as day,
    host_name as host_name,
    user_name as user_name,
    user_id as user_sid,
    application_name as application_name,
    action as action,
    log_level as log_level,
    from_resource_name as source_resource_name,
    from_resource_prefix as source_resource_prefix,
    from_resource_path as source_resource_path,
    from_resource_short_name as source_resource_short_name,
    from_resource_owner_id as source_resource_owner_id,
    from_resource_created_date as source_resource_create_date,
    to_resource_name as target_resource_name
  FROM report_tracking_activity_log;

CREATE VIEW tracking_custom_attribute_v1 AS
  SELECT 
    id as id,
    tracking_log_id as tracking_log_id,
    attribute_name as attribute_name,
    attribute_value as attribute_value
  FROM report_tracking_custom_attr;

CREATE VIEW policy_log_v2 AS
  SELECT 
    id as id,
    time as timestamp,
    month_nb as month,
    day_nb as day,
	host_id as host_id,
	host_ip as host_ip,	
    host_name as host_name,
	user_id as user_id,
    user_name as user_name,
    user_sid as user_sid,
	application_id as application_id,
    application_name as application_name,
    action as action,
	policy_id as policy_id,
    policy_fullname as policy_full_name,
	policy_name as policy_name,
    policy_decision as policy_decision,
	decision_request_id as decision_request_id,
    log_level as log_level,
    from_resource_name as source_resource_name,
	from_resource_size as source_resource_size,
    from_resource_prefix as source_resource_prefix,
    from_resource_path as source_resource_path,
    from_resource_short_name as source_resource_short_name,
    from_resource_owner_id as source_resource_owner_id,
    from_resource_created_date as source_resource_create_date,
	from_resource_modified_date as source_resource_modified_date,
    to_resource_name as target_resource_name,
	attr1 as attr1,
	attr2 as attr2,
	attr3 as attr3,
	attr4 as attr4,
	attr5 as attr5,
	attr6 as attr6,
	attr7 as attr7,
	attr8 as attr8,
	attr9 as attr9,
	attr10 as attr10,
	attr11 as attr11,
	attr12 as attr12,
	attr13 as attr13,
	attr14 as attr14,
	attr15 as attr15,
	attr16 as attr16,
	attr17 as attr17,
	attr18 as attr18,
	attr19 as attr19,
	attr20 as attr20,
	attr21 as attr21,
	attr22 as attr22,
	attr23 as attr23,
	attr24 as attr24,
	attr25 as attr25,
	attr26 as attr26,
	attr27 as attr27,
	attr28 as attr28,
	attr29 as attr29,
	attr30 as attr30,
	attr31 as attr31,
	attr32 as attr32,
	attr33 as attr33,
	attr34 as attr34,
	attr35 as attr35,
	attr36 as attr36,
	attr37 as attr37,
	attr38 as attr38,
	attr39 as attr39,
	attr40 as attr40,
	attr41 as attr41,
	attr42 as attr42,
	attr43 as attr43,
	attr44 as attr44,
	attr45 as attr45,
	attr46 as attr46,
	attr47 as attr47,
	attr48 as attr48,
	attr49 as attr49,
	attr50 as attr50,
	attr51 as attr51,
	attr52 as attr52,
	attr53 as attr53,
	attr54 as attr54,
	attr55 as attr55,
	attr56 as attr56,
	attr57 as attr57,
	attr58 as attr58,
	attr59 as attr59,
	attr60 as attr60,
	attr61 as attr61,
	attr62 as attr62,
	attr63 as attr63,
	attr64 as attr64,
	attr65 as attr65,
	attr66 as attr66,
	attr67 as attr67,
	attr68 as attr68,
	attr69 as attr69,
	attr70 as attr70,
	attr71 as attr71,
	attr72 as attr72,
	attr73 as attr73,
	attr74 as attr74,
	attr75 as attr75,
	attr76 as attr76,
	attr77 as attr77,
	attr78 as attr78,
	attr79 as attr79,
	attr80 as attr80,
	attr81 as attr81,
	attr82 as attr82,
	attr83 as attr83,
	attr84 as attr84,
	attr85 as attr85,
	attr86 as attr86,
	attr87 as attr87,
	attr88 as attr88,
	attr89 as attr89,
	attr90 as attr90,
	attr91 as attr91,
	attr92 as attr92,
	attr93 as attr93,
	attr94 as attr94,
	attr95 as attr95,
	attr96 as attr96,
	attr97 as attr97,
	attr98 as attr98,
	attr99 as attr99
  FROM rpa_log;

CREATE VIEW policy_custom_attribute_v2 AS
  SELECT 
    policy_log_id as policy_log_id,
    attr_id as attr_id,
    attr_value as attr_value
  FROM rpa_log_attr;

CREATE VIEW policy_attr_mapping_v2 AS 
  SELECT 
    mapped_column as mapped_column,
    name as name,
    data_type as data_type,
    attr_type as attr_type 
  FROM rpa_log_mapping