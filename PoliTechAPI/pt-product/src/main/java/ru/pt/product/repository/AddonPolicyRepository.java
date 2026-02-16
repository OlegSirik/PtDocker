package ru.pt.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.pt.product.entity.AddonPolicyEntity;

import java.util.List;
import java.util.UUID;

public interface AddonPolicyRepository extends JpaRepository<AddonPolicyEntity, Long> {

    List<AddonPolicyEntity> findByPolicyIdOrderById(UUID policyId);

    @Query("select ap from AddonPolicyEntity ap where ap.policyId = :policyId and ap.addonId = :addonId")
    List<AddonPolicyEntity> findByPolicyIdAndAddonId(@Param("policyId") UUID policyId, @Param("addonId") Long addonId);
}
