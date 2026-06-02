package com.example.sclms_school_service.Specification;

import com.example.sclms_school_service.DTO.FilterSchoolDTO;
import com.example.sclms_school_service.Model.School;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SchoolSpecification {
//crate coustom sql query by give arguments
    /*
    * ex-:
    SELECT *
    FROM school
    WHERE district = 'Colombo'
    AND type = 'National'
    AND st_count >= 50
    AND st_count <= 200
    AND is_primary_school = true;
    * */
    public static Specification<School> filterBy(FilterSchoolDTO filter) {
        return (Root<School> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
            }
            if (filter.getAddress() != null && !filter.getAddress().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("address")), "%" + filter.getAddress().toLowerCase() + "%"));
            }
            if (filter.getProvince() != null && !filter.getProvince().isEmpty()) {
                predicates.add(cb.equal(root.get("province"), filter.getProvince()));
            }
            if (filter.getDistrict() != null && !filter.getDistrict().isEmpty()) {
                predicates.add(cb.equal(root.get("district"), filter.getDistrict()));
            }
            if (filter.getType() != null && !filter.getType().isEmpty()) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getMinStudentCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("stCount"), filter.getMinStudentCount()));
            }
            if (filter.getMaxStudentCount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("stCount"), filter.getMaxStudentCount()));
            }
            if (filter.getMinTeacherCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("techCount"), filter.getMinTeacherCount()));
            }
            if (filter.getMaxTeacherCount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("techCount"), filter.getMaxTeacherCount()));
            }
            if (filter.getIsSportSchool() != null) {
                predicates.add(cb.equal(root.get("isSportSchool"), filter.getIsSportSchool()));
            }
            if (filter.getIsPrimarySchool() != null) {
                predicates.add(cb.equal(root.get("isPrimarySchool"), filter.getIsPrimarySchool()));
            }
            if (filter.getIsPoshkaSchool() != null) {
                predicates.add(cb.equal(root.get("isPoshkaSchool"), filter.getIsPoshkaSchool()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
