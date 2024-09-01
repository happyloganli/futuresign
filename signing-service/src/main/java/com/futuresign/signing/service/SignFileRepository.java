package com.futuresign.signing.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SignFileRepository extends JpaRepository<SignFile, String> {}
