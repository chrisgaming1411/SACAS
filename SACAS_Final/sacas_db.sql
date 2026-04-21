-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 21, 2026 at 11:25 AM
-- Server version: 10.4.21-MariaDB
-- PHP Version: 8.0.11

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sacas_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `appointments`
--

CREATE TABLE `appointments` (
  `id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `instructor_id` int(11) NOT NULL,
  `day_of_week` varchar(15) NOT NULL,
  `time_slot` varchar(30) NOT NULL,
  `reason` text DEFAULT NULL,
  `status` enum('PENDING','CONFIRMED','DECLINED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `appointments`
--

INSERT INTO `appointments` (`id`, `student_id`, `instructor_id`, `day_of_week`, `time_slot`, `reason`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 'Monday', '10:00 A.M', 'LACKINGS', 'CONFIRMED', '2026-04-21 09:14:26', '2026-04-21 09:15:17');

-- --------------------------------------------------------

--
-- Table structure for table `availability`
--

CREATE TABLE `availability` (
  `id` int(11) NOT NULL,
  `instructor_id` int(11) NOT NULL,
  `day_of_week` varchar(15) NOT NULL,
  `time_slot` varchar(20) NOT NULL,
  `is_recurring` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `availability`
--

INSERT INTO `availability` (`id`, `instructor_id`, `day_of_week`, `time_slot`, `is_recurring`) VALUES
(1, 1, 'Monday', '7:00 A.M', 1),
(2, 1, 'Monday', '10:00 A.M', 1),
(3, 1, 'Monday', '2:00 P.M', 1),
(4, 1, 'Monday', '4:00 P.M', 1);

-- --------------------------------------------------------

--
-- Table structure for table `consultation_logs`
--

CREATE TABLE `consultation_logs` (
  `id` int(11) NOT NULL,
  `appointment_id` int(11) DEFAULT NULL,
  `student_name` varchar(120) DEFAULT NULL,
  `instructor_name` varchar(120) DEFAULT NULL,
  `subject` varchar(100) DEFAULT NULL,
  `day_of_week` varchar(15) DEFAULT NULL,
  `time_slot` varchar(30) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `logged_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `consultation_logs`
--

INSERT INTO `consultation_logs` (`id`, `appointment_id`, `student_name`, `instructor_name`, `subject`, `day_of_week`, `time_slot`, `status`, `logged_at`) VALUES
(1, 1, 'Albert Daro', 'Rey Mark Cajes', NULL, 'Monday', '10:00 A.M', 'CONFIRMED', '2026-04-21 09:15:17');

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `name` varchar(50) NOT NULL,
  `code` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`name`, `code`) VALUES
('Bachelor of Science in Information Technlogy', 'BSIT');

-- --------------------------------------------------------

--
-- Table structure for table `instructors`
--

CREATE TABLE `instructors` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `instructor_no` varchar(20) NOT NULL,
  `firstname` varchar(60) NOT NULL,
  `middlename` varchar(60) DEFAULT NULL,
  `lastname` varchar(60) NOT NULL,
  `suffix` varchar(10) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `birthdate` varchar(20) DEFAULT NULL,
  `course` varchar(100) DEFAULT NULL,
  `schedule` varchar(50) DEFAULT NULL,
  `timeslot` varchar(50) DEFAULT NULL,
  `subject1` varchar(100) DEFAULT NULL,
  `subject2` varchar(100) DEFAULT NULL,
  `subject3` varchar(100) DEFAULT NULL,
  `subject4` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `instructors`
--

INSERT INTO `instructors` (`id`, `user_id`, `instructor_no`, `firstname`, `middlename`, `lastname`, `suffix`, `address`, `age`, `birthdate`, `course`, `schedule`, `timeslot`, `subject1`, `subject2`, `subject3`, `subject4`) VALUES
(1, 3, 'INS-2026-001', 'Rey Mark', 'Hoylar', 'Cajes', '', 'Naupa', 20, '20058/05/06', 'BS Information Technology', 'Monday - Wednesday - Friday', '10:30 AM - 12:00 PM', 'asdasds', 'adasdas', 'asdasd', 'asdasd');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `message`, `is_read`, `created_at`) VALUES
(1, 3, 'New appointment request from Albert Daro on Monday at 10:00 A.M. Reason: LACKINGS', 0, '2026-04-21 09:14:26'),
(2, 2, '✅ Your appointment request on Monday at 10:00 A.M with Rey Mark Cajes has been CONFIRMED.', 1, '2026-04-21 09:15:17');

-- --------------------------------------------------------

--
-- Table structure for table `students`
--

CREATE TABLE `students` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `student_no` varchar(20) NOT NULL,
  `firstname` varchar(60) NOT NULL,
  `middlename` varchar(60) DEFAULT NULL,
  `lastname` varchar(60) NOT NULL,
  `suffix` varchar(10) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `birthdate` varchar(20) DEFAULT NULL,
  `mothers_name` varchar(100) DEFAULT NULL,
  `mothers_occ` varchar(100) DEFAULT NULL,
  `fathers_name` varchar(100) DEFAULT NULL,
  `fathers_occ` varchar(100) DEFAULT NULL,
  `elementary` varchar(100) DEFAULT NULL,
  `junior_high` varchar(100) DEFAULT NULL,
  `senior_high` varchar(100) DEFAULT NULL,
  `course` varchar(100) DEFAULT NULL,
  `schedule` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `students`
--

INSERT INTO `students` (`id`, `user_id`, `student_no`, `firstname`, `middlename`, `lastname`, `suffix`, `address`, `age`, `birthdate`, `mothers_name`, `mothers_occ`, `fathers_name`, `fathers_occ`, `elementary`, `junior_high`, `senior_high`, `course`, `schedule`) VALUES
(1, 2, 'STU-2026-001', 'Albert', 'Auxtero', 'Daro', 'Jr.', 'Prk.2', 17, '2009/01/06', 'Daro', 'housewife', 'Alberto', 'Driver', 'ashdjas', 'jkashkjhas', 'aksjdhkjashd', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(60) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','STUDENT','INSTRUCTOR') NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `role`, `created_at`) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', '2026-04-21 08:31:48'),
(2, 'adaro001', '3e252948f0c5c332713e511e25caae8eb415b7666e48cc96409b6b093edbd050', 'STUDENT', '2026-04-21 09:09:48'),
(3, 'rcajes001', '3af67dad3168e6cfc89799185094870a357b4b457236095f08673a49773e8519', 'INSTRUCTOR', '2026-04-21 09:11:12');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_appointments`
-- (See below for the actual view)
--
CREATE TABLE `v_appointments` (
`id` int(11)
,`status` enum('PENDING','CONFIRMED','DECLINED','CANCELLED','COMPLETED')
,`day_of_week` varchar(15)
,`time_slot` varchar(30)
,`reason` text
,`created_at` timestamp
,`student_no` varchar(20)
,`student_name` varchar(121)
,`student_course` varchar(100)
,`instructor_no` varchar(20)
,`instructor_name` varchar(121)
,`instructor_dept` varchar(100)
);

-- --------------------------------------------------------

--
-- Structure for view `v_appointments`
--
DROP TABLE IF EXISTS `v_appointments`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_appointments`  AS SELECT `a`.`id` AS `id`, `a`.`status` AS `status`, `a`.`day_of_week` AS `day_of_week`, `a`.`time_slot` AS `time_slot`, `a`.`reason` AS `reason`, `a`.`created_at` AS `created_at`, `s`.`student_no` AS `student_no`, concat(`s`.`firstname`,' ',`s`.`lastname`) AS `student_name`, `s`.`course` AS `student_course`, `i`.`instructor_no` AS `instructor_no`, concat(`i`.`firstname`,' ',`i`.`lastname`) AS `instructor_name`, `i`.`course` AS `instructor_dept` FROM ((`appointments` `a` join `students` `s` on(`a`.`student_id` = `s`.`id`)) join `instructors` `i` on(`a`.`instructor_id` = `i`.`id`)) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `student_id` (`student_id`),
  ADD KEY `instructor_id` (`instructor_id`);

--
-- Indexes for table `availability`
--
ALTER TABLE `availability`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_avail` (`instructor_id`,`day_of_week`,`time_slot`);

--
-- Indexes for table `consultation_logs`
--
ALTER TABLE `consultation_logs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `instructors`
--
ALTER TABLE `instructors`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `instructor_no` (`instructor_no`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `students`
--
ALTER TABLE `students`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `student_no` (`student_no`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `appointments`
--
ALTER TABLE `appointments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `availability`
--
ALTER TABLE `availability`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `consultation_logs`
--
ALTER TABLE `consultation_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `instructors`
--
ALTER TABLE `instructors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `students`
--
ALTER TABLE `students`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `appointments`
--
ALTER TABLE `appointments`
  ADD CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`instructor_id`) REFERENCES `instructors` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `availability`
--
ALTER TABLE `availability`
  ADD CONSTRAINT `availability_ibfk_1` FOREIGN KEY (`instructor_id`) REFERENCES `instructors` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `instructors`
--
ALTER TABLE `instructors`
  ADD CONSTRAINT `instructors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `students`
--
ALTER TABLE `students`
  ADD CONSTRAINT `students_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
