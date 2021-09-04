package org.jpwh.web.dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.SingularAttribute;

public class SeekPage extends Page {

    /* 
        The seek technique requires an additional paging attribute, besides
        the regular sorting attribute, that is a guaranteed unique key.
        This can be any unique attribute of your entity model, but is most
        of the time the primary key attribute.
     */
    protected SingularAttribute uniqueAttribute;

    /* 
        For both the sorting attribute and the unique key attribute, you must
        remember their values from the "last page". You can then retrieve the
        next page by seeking those values. Any <code>Comparable</code> value
        is fine, as required by the restriction API in criteria queries.
     */
    protected Comparable lastValue;
    protected Comparable lastUniqueValue;

    public SeekPage(int size,
                    long totalRecords,
                    SingularAttribute defaultAttribute,
                    SortDirection defaultDirection,
                    SingularAttribute uniqueAttribute,
                    SingularAttribute... allowedAttributes) {
        super(size, totalRecords, defaultAttribute, defaultDirection, allowedAttributes);
        this.uniqueAttribute = uniqueAttribute;
    }

    public SingularAttribute getUniqueAttribute() {
        return uniqueAttribute;
    }

    public void setUniqueAttribute(SingularAttribute uniqueAttribute) {
        this.uniqueAttribute = uniqueAttribute;
    }

    public Comparable getLastValue() {
        return lastValue;
    }

    public void setLastValue(Comparable lastValue) {
        this.lastValue = lastValue;
    }

    public Comparable getLastUniqueValue() {
        return lastUniqueValue;
    }

    public void setLastUniqueValue(Comparable lastUniqueValue) {
        this.lastUniqueValue = lastUniqueValue;
    }

    public boolean isApplicableFor(Bindable bindable) {
        return super.isApplicableFor(bindable)
            && isAttributeDeclaredIn(getUniqueAttribute(), bindable);
    }

    public boolean isFirst() {
        return getLastValue() == null || getLastUniqueValue() == null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(EntityManager em,
                                         CriteriaQuery<T> criteriaQuery,
                                         Path attributePath) {

        throwIfNotApplicableFor(attributePath);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        /* 
            You must always sort the result by both the sorting
            and the unique key attribute.
         */
        Path sortPath = attributePath.get(getSortAttribute());
        Path uniqueSortPath = attributePath.get(getUniqueAttribute());
        if (isSortedAscending()) {
            criteriaQuery.orderBy(cb.asc(sortPath), cb.asc(uniqueSortPath));
        } else {
            criteriaQuery.orderBy(cb.desc(sortPath), cb.desc(uniqueSortPath));
        }

        /* 
            Add any necessary additional restrictions (not shown) to the
            <code>where</code> clause of the query, seeking beyond the last
            known values to the target page.
         */
        applySeekRestriction(em, criteriaQuery, attributePath);

        TypedQuery<T> query = em.createQuery(criteriaQuery);

        /* 
            Cut the result off with the desired page size.
         */
        if (getSize() != -1)
            query.setMaxResults(getSize());

        return query;
    }

    protected void applySeekRestriction(EntityManager em,
                                        CriteriaQuery criteriaQuery,
                                        Path attributePath) {
        // Don't have to seek anywhere if we are on the first page
        if (isFirst())
            return;

        applySeekRestriction(
            em,
            criteriaQuery,
            attributePath,
            em.getCriteriaBuilder().literal(getLastValue()),
            em.getCriteriaBuilder().literal(getLastUniqueValue())
        );
    }

    protected void applySeekRestriction(EntityManager em,
                                        AbstractQuery criteriaQuery,
                                        Path attributePath,
                                        Expression lastValueExpression,
                                        Expression lastUniqueValueExpression) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        criteriaQuery.where(
            cb.and(
                (isSortedAscending()
                    ? cb.greaterThanOrEqualTo(
                    attributePath.get(getSortAttribute()),
                    lastValueExpression)
                    : cb.lessThanOrEqualTo(
                    attributePath.get(getSortAttribute()),
                    lastValueExpression)
                ),
                cb.or(
                    cb.notEqual(
                        attributePath.get(getSortAttribute()),
                        lastValueExpression
                    ),
                    (isSortedAscending()
                        ? cb.greaterThan(
                        attributePath.get(getUniqueAttribute()),
                        lastUniqueValueExpression)
                        : cb.lessThan(
                        attributePath.get(getUniqueAttribute()),
                        lastUniqueValueExpression)
                    )
                )
            )
        );
    }
}
