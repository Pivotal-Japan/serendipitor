CREATE TABLE slack_user (
  user_id   VARCHAR(128) NOT NULL,
  user_name VARCHAR(128) NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE one_on_one (
  first  VARCHAR(128) NOT NULL,
  second VARCHAR(128) NOT NULL,
  date   DATE         NOT NULL,
  PRIMARY KEY (first, second, date)
);
