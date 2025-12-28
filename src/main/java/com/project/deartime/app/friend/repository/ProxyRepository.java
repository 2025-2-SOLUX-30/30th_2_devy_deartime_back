package com.project.deartime.app.friend.repository;

import com.project.deartime.app.domain.Proxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProxyRepository extends JpaRepository<Proxy, Long> {

    @Query("SELECT p FROM Proxy p WHERE p.user.id = :userId AND p.proxyUser.id = :proxyUserId")
    List<Proxy> findByUserIdAndProxyUserId(@Param("userId") Long userId,
                                           @Param("proxyUserId") Long proxyUserId);

    @Query("SELECT p FROM Proxy p WHERE p.user.id = :userId")
    List<Proxy> findByUserId(@Param("userId") Long userId);
}