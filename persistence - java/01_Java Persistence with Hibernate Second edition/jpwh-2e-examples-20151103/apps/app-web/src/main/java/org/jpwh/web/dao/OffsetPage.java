package org.jpwh.web.dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;
import java.math.BigDecimal;

public class OffsetPage extends Page {

    /* 
        For offset-based paging, you need to know on which page you are. By default, we start with page 1.
     */
    protected int current = 1;

    public OffsetPage(int size, 
                      long totalRecords,
                      SingularAttribute defaultAttribute,
                      SortDirection defaultDirection, 
                      SingularAttribute... allowedAttributes) {
        super(size, totalRecords, defaultAttribute, defaultDirection, allowedAttributes);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getNext() {
        return getCurrent() + 1;
    }

    public int getPrevious() {
        return getCurrent() - 1;
    }

    public int getFirst() {
        return 1;
    }

    public long getLast() {
        long lastPage = (getTotalRecords() / getSize());
        if (getTotalRecords() % getSize() == 0)
            lastPage--;
        return lastPage + 1;
    }

    public long getRangeStart() {
        return (getCurrent() - 1) * getSize();
    }

    public int getRangeStartInteger() throws ArithmeticException {
        return new BigDecimal(getRangeStart()).intValueExact();
    }

    public long getRangeEnd() {
        long firstIndex = getRangeStart();
        long pageIndex = getSize() - 1;
        long lastIndex = Math.max(0, getTotalRecords() - 1);
        return Math.min(firstIndex + pageIndex, lastIndex);
    }

    public int getRangeEndInteger() throws ArithmeticException {
        return new BigDecimal(getRangeEnd()).intValueExact();
    }

    public boolean isPreviousAvailable() {
        return getRangeStart() + 1 > getSize();
    }

    public boolean isNextAvailable() {
        return getTotalRecords() - 1 > getRangeEnd();
    }

    @Override
    public <T> TypedQuery<T> createQuery(EntityManager em,
                                         CriteriaQuery<T> criteriaQuery,
                                         Path attributePath) {

        /* 
            Test if the sorting attribute of this page can be resolved
            against the attribute path and therefore the model used by
            the query. The method throws an exception if the sorting attribute
            of the page wasn't available on the model class referenced in
            the query. This is a safety mechanism that will produce a
            meaningful error message if you pair the wrong paging settings
            with the wrong query.
         */
        throwIfNotApplicableFor(attributePath);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        /* 
            Add an <code>ORDER BY</code> clause to the query.
         */
        Path sortPath = attributePath.get(getSortAttribute());
        criteriaQuery.orderBy(
            isSortedAscending() ? cb.asc(sortPath) : cb.desc(sortPath)
        );

        TypedQuery<T> query = em.createQuery(criteriaQuery);

        /* 
            Set the offset of the query, the starting result row.
         */
        query.setFirstResult(getRangeStartInteger());

        /* 
            Cut the result off with the desired page size.
         */
        if (getSize() != -1)
            query.setMaxResults(getSize());

        return query;
    }
}