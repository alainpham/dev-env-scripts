create table IF NOT EXISTS account_balance (
	id SERIAL NOT NULL PRIMARY KEY,
	current_balance DECIMAL,
    currency VARCHAR,
	create_date TIMESTAMP,
	update_date TIMESTAMP
);