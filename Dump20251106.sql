-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: q_authent
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_policies`
--

DROP TABLE IF EXISTS `account_policies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_policies` (
  `policy_id` varchar(255) NOT NULL,
  `target_account_id` varchar(255) NOT NULL,
  `root_id` varchar(255) NOT NULL,
  `creator_id` varchar(255) NOT NULL,
  `last_edit_id` varchar(255) NOT NULL,
  `can_create` bit(1) NOT NULL DEFAULT b'0',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `del_flg` bit(1) NOT NULL DEFAULT b'0',
  `can_view` bit(1) DEFAULT NULL,
  `can_delete` bit(1) DEFAULT NULL,
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `policy_id` (`policy_id`),
  KEY `FK1g1xvibsm3g0v8xadett342a1` (`root_id`),
  KEY `FKcbgoexqjuhpicogc1twtk6l5n` (`target_account_id`),
  KEY `FKdntlwjkdp295u7iqfw67q37y2` (`last_edit_id`),
  KEY `FKdvfmouvr7eomwak6n3nlpxydn` (`creator_id`),
  CONSTRAINT `FK1g1xvibsm3g0v8xadett342a1` FOREIGN KEY (`root_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `FKcbgoexqjuhpicogc1twtk6l5n` FOREIGN KEY (`target_account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `FKdntlwjkdp295u7iqfw67q37y2` FOREIGN KEY (`last_edit_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `FKdvfmouvr7eomwak6n3nlpxydn` FOREIGN KEY (`creator_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_policies`
--

LOCK TABLES `account_policies` WRITE;
/*!40000 ALTER TABLE `account_policies` DISABLE KEYS */;
INSERT INTO `account_policies` VALUES ('APCb52917c9b18c4654b0a1859afef14e79','ACCeda4237a99cf41dd834e3803c0534b3c','ACCc90b395fbb4740ea893b449c0b765655','ACC8e6a7d035e2b4b58af135b8f2aac1f9d','ACC8e6a7d035e2b4b58af135b8f2aac1f9d',_binary '','2025-11-02 05:28:25','2025-11-02 05:28:25',_binary '\0',_binary '\0',_binary '\0'),('UPC120c5d16900443418ad614830fc04e4c','ACC6756b756fcc54016a9da0cec0a686e0a','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655',_binary '\0','2025-10-29 07:34:25','2025-10-29 07:34:25',_binary '\0',_binary '\0',_binary '\0'),('UPC16a052216897482a8da0df495dc15784','ACC08ceb0e9b5324d3bbd233b1837980f99','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655',_binary '\0','2025-10-29 07:31:37','2025-10-29 07:31:37',_binary '\0',_binary '\0',_binary '\0'),('UPC492da3a3927d4a81b4c0001a3b5f5e31','ACC8e6a7d035e2b4b58af135b8f2aac1f9d','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655',_binary '','2025-10-27 11:46:02','2025-11-02 05:27:31',_binary '\0',_binary '\0',_binary '\0'),('UPC5e0b64af391b4c2797e3a0221e245e83','ACC372e1c74a5fc49118ae7ce51cac9071e','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655',_binary '','2025-10-23 15:35:30','2025-10-31 13:51:39',_binary '\0',_binary '\0',_binary ''),('UPCfcb90cfea9d148bd8d9f989d92f03d87','ACC320c6974f15f443b83a5bb35f8177e7c','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655',_binary '','2025-10-29 07:29:09','2025-10-29 07:29:09',_binary '\0',_binary '',_binary '');
/*!40000 ALTER TABLE `account_policies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_settings`
--

DROP TABLE IF EXISTS `account_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_settings` (
  `setting_id` varchar(255) NOT NULL,
  `account_id` varchar(255) NOT NULL,
  `setting_values` mediumtext,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `del_flag` bit(1) DEFAULT NULL,
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `setting_id` (`setting_id`),
  KEY `account_settings_index_0` (`account_id`),
  CONSTRAINT `account_settings_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_settings`
--

LOCK TABLES `account_settings` WRITE;
/*!40000 ALTER TABLE `account_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `accounts`
--

DROP TABLE IF EXISTS `accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounts` (
  `account_id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `del_flag` bit(1) DEFAULT NULL,
  `root_id` varchar(255) DEFAULT NULL,
  `parent_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `account_id` (`account_id`),
  KEY `accounts_index_0` (`username`),
  KEY `accounts_index_1` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounts`
--

LOCK TABLES `accounts` WRITE;
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT INTO `accounts` VALUES ('ACC08ceb0e9b5324d3bbd233b1837980f99','niga-man','$2a$10$xwCcaPXC7D0QM9WOUb7ZKevFXKZhPLU8taKEboDgpTzAx/czDtpWS','maiaa@maiak.com','niga-man',NULL,_binary '\0','2025-10-29 07:31:37','2025-10-29 07:31:37',_binary '','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655'),('ACC320c6974f15f443b83a5bb35f8177e7c','niga-user','$2a$10$4qPgty19c8Fx.QiSA/Ki3.LrdlG3leVWdc/tNpmoqcvjp81H58SYW','test@gaim.niga','niga-user',NULL,_binary '\0','2025-10-29 07:29:08','2025-10-29 07:29:08',_binary '','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655'),('ACC372e1c74a5fc49118ae7ce51cac9071e','sub1','$2a$10$vaav8edw0EROKvs.BELwbuA2NoCjk2ZX1Y90eLhcBeGezjuFU7Dqe','mail@test.com','sub1',NULL,_binary '','2025-10-23 15:35:30','2025-10-23 15:35:30',_binary '','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655'),('ACC6756b756fcc54016a9da0cec0a686e0a','supa-niga','$2a$10$RTrNDE3CK7MBnCsDd4mNKeZFnnuzaFT8yyHb0GT5QMAqC7Ybb5fw2','man@niga.com','supa-niga',NULL,_binary '\0','2025-10-29 07:34:25','2025-10-29 07:34:25',_binary '','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655'),('ACC8e6a7d035e2b4b58af135b8f2aac1f9d','sub2','$2a$10$7GcAwbXmLMmhzH6tvXR1AOvBnGZ7a8c24eS4ZskpAHP3dYUZ73/p6','sub2@mail.com','Daisy',NULL,_binary '','2025-10-27 11:46:02','2025-10-27 11:46:02',_binary '\0','ACCc90b395fbb4740ea893b449c0b765655','ACCc90b395fbb4740ea893b449c0b765655'),('ACCb036829a97704281a10f7cd7bbb022d4','leequan','$2a$10$6/wcCEcRzP830RhNxmtr8uzCoRPGmz/b3vO7rrOzsEsMXkzfcvMIy','leequan123@gmail.com','lee',NULL,_binary '',NULL,NULL,NULL,NULL,NULL),('ACCc90b395fbb4740ea893b449c0b765655','leequanno1','$2a$10$QkijRzjbk2cDLxiVZt6yj.4Bh8F/hrtUoj76DhfeVRAfr/2CG1Q0K','binquan1234@gmail.com','leequanno1',NULL,_binary '',NULL,NULL,NULL,NULL,NULL),('ACCeda4237a99cf41dd834e3803c0534b3c','sub1sub','$2a$10$sJUHceQVgKX/BqewUWQYb.hufHnNeDjoOkbaD.MaRByTFu3viT2ya','test2@email.com','sub1sub',NULL,_binary '','2025-11-02 05:28:25','2025-11-02 05:28:25',_binary '\0','ACCc90b395fbb4740ea893b449c0b765655','ACC8e6a7d035e2b4b58af135b8f2aac1f9d');
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `oauth2_agents`
--

DROP TABLE IF EXISTS `oauth2_agents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oauth2_agents` (
  `oauth2_cd` int NOT NULL AUTO_INCREMENT,
  `agent_name` varchar(100) NOT NULL,
  PRIMARY KEY (`oauth2_cd`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth2_agents`
--

LOCK TABLES `oauth2_agents` WRITE;
/*!40000 ALTER TABLE `oauth2_agents` DISABLE KEYS */;
/*!40000 ALTER TABLE `oauth2_agents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pool_oauth2_register`
--

DROP TABLE IF EXISTS `pool_oauth2_register`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pool_oauth2_register` (
  `oauth2_register_id` varchar(255) NOT NULL,
  `pool_id` varchar(255) NOT NULL,
  `oauth2_cd` int NOT NULL,
  `client_id` varchar(191) NOT NULL,
  `client_secret` varchar(512) NOT NULL,
  `redirect_uri` varchar(2048) NOT NULL,
  `optional_data` varchar(5000) DEFAULT '{}',
  `last_edit_account_id` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`oauth2_register_id`),
  KEY `pool_oauth2_register_ibfk_1` (`last_edit_account_id`),
  KEY `pool_oauth2_register_ibfk_2` (`pool_id`),
  KEY `pool_oauth2_register_ibfk_3` (`oauth2_cd`),
  CONSTRAINT `pool_oauth2_register_ibfk_1` FOREIGN KEY (`last_edit_account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `pool_oauth2_register_ibfk_2` FOREIGN KEY (`pool_id`) REFERENCES `user_pools` (`pool_id`) ON DELETE CASCADE,
  CONSTRAINT `pool_oauth2_register_ibfk_3` FOREIGN KEY (`oauth2_cd`) REFERENCES `oauth2_agents` (`oauth2_cd`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pool_oauth2_register`
--

LOCK TABLES `pool_oauth2_register` WRITE;
/*!40000 ALTER TABLE `pool_oauth2_register` DISABLE KEYS */;
/*!40000 ALTER TABLE `pool_oauth2_register` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_pool_policies`
--

DROP TABLE IF EXISTS `user_pool_policies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_pool_policies` (
  `policy_id` varchar(255) NOT NULL,
  `account_id` varchar(255) NOT NULL,
  `root_id` varchar(255) NOT NULL,
  `creator_id` varchar(255) NOT NULL,
  `last_edit_id` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `del_flg` bit(1) NOT NULL,
  `pool_id` varchar(255) NOT NULL,
  `can_view` bit(1) DEFAULT NULL,
  `can_edit` bit(1) DEFAULT NULL,
  `can_manage` bit(1) DEFAULT NULL,
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `policy_id` (`policy_id`),
  KEY `last_edit_id` (`last_edit_id`),
  KEY `FK8wyslriudo0ws921l2d9110a2` (`creator_id`),
  KEY `FKlq7ewchqofk1e3lkdr48o3rik` (`root_id`),
  KEY `FKorm3aewiqjsnmsgqbevfr1xyp` (`account_id`),
  KEY `user_pool_policies_ibfk_2` (`pool_id`),
  CONSTRAINT `FK8wyslriudo0ws921l2d9110a2` FOREIGN KEY (`creator_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `FKlq7ewchqofk1e3lkdr48o3rik` FOREIGN KEY (`root_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `FKorm3aewiqjsnmsgqbevfr1xyp` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `user_pool_policies_ibfk_1` FOREIGN KEY (`last_edit_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE,
  CONSTRAINT `user_pool_policies_ibfk_2` FOREIGN KEY (`pool_id`) REFERENCES `user_pools` (`pool_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_pool_policies`
--

LOCK TABLES `user_pool_policies` WRITE;
/*!40000 ALTER TABLE `user_pool_policies` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_pool_policies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_pools`
--

DROP TABLE IF EXISTS `user_pools`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_pools` (
  `pool_id` varchar(255) NOT NULL,
  `account_id` varchar(255) NOT NULL,
  `user_fields` varchar(1000) NOT NULL,
  `authorize_fields` varchar(1000) NOT NULL,
  `pool_key` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `del_flag` bit(1) DEFAULT NULL,
  `private_access_key` varchar(660) DEFAULT NULL,
  `private_refresh_key` varchar(660) DEFAULT NULL,
  `pool_name` varchar(255) DEFAULT NULL,
  `email_verify` bit(1) DEFAULT NULL,
  `role_levels` varchar(255) DEFAULT NULL,
  `access_expired_minutes` int DEFAULT NULL,
  `refresh_expired_days` int DEFAULT NULL,
  PRIMARY KEY (`pool_id`),
  UNIQUE KEY `pool_id` (`pool_id`),
  KEY `user_pools_index_0` (`pool_key`),
  KEY `user_pools_index_1` (`account_id`),
  CONSTRAINT `user_pools_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_pools`
--

LOCK TABLES `user_pools` WRITE;
/*!40000 ALTER TABLE `user_pools` DISABLE KEYS */;
INSERT INTO `user_pools` VALUES ('UPLbcf082c26deb46828d981667be9584db','ACCc90b395fbb4740ea893b449c0b765655','[\"username\",\"password\",\"email\"]','[\"username\",\"password\"]','i2YuBRWnBtOyKrXtlGoEfg==','2025-11-06 05:35:48','2025-11-06 05:35:48',_binary '\0','YZkkAn2YclbSZomUXlh5W3VIolma3Uf2z7MzdPFjG/klWFC4l1bzXA/1vSKhoXfqBmCLr8wInEX+TfOui4xjTSAmxYGVEnKXyRDlDPmkonA+YM9+Af0yXCZiBUYABcAOT2IHQNIVAH281xFcHc7qbhqwFow=','Z5cGaKb9qx5NkJ7AcALleroMGCQIPYjHn48Rwh7bnjYUbHGH2dnqbmmVZFENvdDrBlF0EDxoZY7DokEo4AQ3bNJnBIqFkgyDjAm7whTekUM4MHHGJOaG5k9xZ3wzOyHA/z+b5h3x37hqteQ0GAbozLK1iCE=','pool1',_binary '\0','null',2,7),('UPLc4c6034c3ea1495ab8997e2ff31c7edf','ACCc90b395fbb4740ea893b449c0b765655','[\"username\",\"password\",\"email\",\"telCountryCode\",\"displayName\",\"createdAt\",\"lastName\",\"phoneNumber\",\"firstName\",\"avatarImg\",\"backgroundImg\",\"gender\",\"updatedAt\"]','[\"username\",\"password\"]','uTXUPZD61VqSoWqu+97/ZQ==','2025-11-06 06:12:08','2025-11-06 06:12:08',_binary '\0','RjWWYxPLg9z2LKT6PfuuXHrwYOKmGa5lrCW8CIAjEzLvW9WUssiMxS6AtH4LdbW8DEOeZB2PDqFE3VSMdnFUG4c6GcKfRW6gaHEZK/HjkG9oue3guTMwc4fGEVXp6lrRz4Gkl3H9Vl+M6/KYBcNCtAoe0p8=','1Uz1QUUpcjAKcAXGdW/+i57udgOMxN9w144ATvM6e7apf4TZf/bnZXPQkzGgfx1l3hmBKhn0LG7JgJjEs08coEFAHVriDIBURpNWxcAR99XnLWBa7n7gYr86KM2eIt0gB1xs3zJpeWpEa6A5lbsMGI1ty2U=','Pool2',_binary '\0','null',2,7);
/*!40000 ALTER TABLE `user_pools` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `validation_code`
--

DROP TABLE IF EXISTS `validation_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `validation_code` (
  `validated_code_id` varchar(255) NOT NULL,
  `target_account_id` varchar(255) NOT NULL,
  `code_value` int NOT NULL,
  `is_used` bit(1) DEFAULT b'0',
  `expire_time` timestamp NOT NULL,
  PRIMARY KEY (`validated_code_id`),
  KEY `validation_code_ibfk_1` (`target_account_id`),
  CONSTRAINT `validation_code_ibfk_1` FOREIGN KEY (`target_account_id`) REFERENCES `accounts` (`account_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `validation_code`
--

LOCK TABLES `validation_code` WRITE;
/*!40000 ALTER TABLE `validation_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `validation_code` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-06 13:16:57
