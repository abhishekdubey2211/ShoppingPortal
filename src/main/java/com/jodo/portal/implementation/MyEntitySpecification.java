package com.jodo.portal.implementation;
import org.springframework.data.jpa.domain.Specification;
import com.jodo.portal.model.FilterCriteria;
import com.jodo.portal.model.Product;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

public class MyEntitySpecification {

    public static Specification<Product> getFilteredSpec(List<FilterCriteria> criteriaList) {
        return (root, query, builder) -> {
            Predicate finalPredicate = null;
            for (FilterCriteria criteria : criteriaList) {
                Predicate predicate = null;
                switch (criteria.getOperation()) {
                    case "=":
                        predicate = builder.equal(root.get(criteria.getFieldName()), criteria.getValue());
                        break;
                    case "!=":
                        predicate = builder.notEqual(root.get(criteria.getFieldName()), criteria.getValue());
                        break;
                    case ">":
                        predicate = builder.greaterThan(root.get(criteria.getFieldName()), criteria.getValue().toString());
                        break;
                    case "<":
                        predicate = builder.lessThan(root.get(criteria.getFieldName()), criteria.getValue().toString());
                        break;
                    case ">=":
                        predicate = builder.greaterThanOrEqualTo(root.get(criteria.getFieldName()), criteria.getValue().toString());
                        break;
                    case "<=":
                        predicate = builder.lessThanOrEqualTo(root.get(criteria.getFieldName()), criteria.getValue().toString());
                        break;
                    case "like":
                        predicate = builder.like(root.get(criteria.getFieldName()), "%" + criteria.getValue() + "%");
                        break;
                    case "in":
                        predicate = root.get(criteria.getFieldName()).in((List<?>) criteria.getValue());
                        break;
                    case "between":
                        predicate = builder.between(root.get(criteria.getFieldName()),
                                criteria.getValue().toString(),
                                criteria.getAdditionalValue().toString());
                        break;
                    default:
                        break;
                }

                // Handle AND/OR conditions
                if (predicate != null) {
                    if (finalPredicate == null) {
                        finalPredicate = predicate;
                    } else if ("AND".equalsIgnoreCase(criteria.getConditionType())) {
                        finalPredicate = builder.and(finalPredicate, predicate);
                    } else if ("OR".equalsIgnoreCase(criteria.getConditionType())) {
                        finalPredicate = builder.or(finalPredicate, predicate);
                    }
                }
            }
            return finalPredicate;
        };
    }
}
