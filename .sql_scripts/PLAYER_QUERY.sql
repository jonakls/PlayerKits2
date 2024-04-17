-- GENERAL QUERY

SELECT pp.UUID,
       pp.PLAYER_NAME,
       ppk.NAME,
       ppk.COOLDOWN,
       ppk.ONE_TIME,
       ppk.BOUGHT
FROM playerkits_players pp
         LEFT JOIN playerkits_players_kits ppk
                   ON pp.UUID = ppk.UUID
WHERE pp.UUID = ?;


-- BASIC TRANSACTIONS FOR PLAYERKITS_PLAYERS

INSERT INTO playerkits_players
VALUES (?, ?);

UPDATE playerkits_players
SET PLAYER_NAME = ?
WHERE UUID = ?;


-- BASIC TRANSACTIONS FOR PLAYERKITS_PLAYERS_KITS

INSERT INTO playerkits_players_kits VALUES (?, ?, ?, ?, ?);

UPDATE playerkits_players_kits
SET COOLDOWN = ?,
    ONE_TIME = ?,
    BOUGHT   = ?
WHERE UUID = ?
  AND NAME = ?;

DELETE
FROM playerkits_players_kits
WHERE UUID = ?
  AND NAME = ? ;