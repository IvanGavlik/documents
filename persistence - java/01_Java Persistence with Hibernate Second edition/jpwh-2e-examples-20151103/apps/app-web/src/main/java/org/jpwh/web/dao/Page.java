package org.jpwh.web.dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Arrays;

public abstract class Page {

    public static enum SortDirection {
        ASC,
        DESC
    }

    /* 
        The model holds the size of each page and the number of records shown per page.
        The value <code>-1</code> is special, meaning "no limit, show all records".
     */
    protected int size = -1;

    /* 
        Keeping the number of total records is necessary for some calculations, for
        example, to determine whether there is actually a "next" page.
     */
    protected long totalRecords;

    /* 
        Paging always requires a deterministic record order; typically, you sort by
        a particular attribute of your entity classes in ascending or descending order.
        The <code>javax.persistence.metamodel.SingularAttribute</code> is an attribute
        of either an entity or an embeddable class in JPA, it's not a collection (you
        can't "order by collection" in a query).
     */
    protected SingularAttribute sortAttribute;
    protected SortDirection sortDirection;

    /* 
        The <code>allowedAttributes</code> list is set when creating the page model; it
        restricts the possible sortable attributes to the ones you can handle in your
        queries.
     */
    protected SingularAttribute[] allowedAttributes;

    protected Page(int size,
                   long totalRecords,
                   SingularAttribute defaultAttribute,
                   SortDirection defaultDirection,
                   SingularAttribute... allowedAttributes) {
        this.size = size;
        this.totalRecords = totalRecords;
        this.sortDirection = defaultDirection;
        this.allowedAttributes = allowedAttributes;
        setSortAttribute(defaultAttribute);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public SingularAttribute[] getAllowedAttributes() {
        return allowedAttributes;
    }

    public SingularAttribute getSortAttribute() {
        return sortAttribute;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public boolean isSortedAscending() {
        return SortDirection.ASC.equals(getSortDirection());
    }

    public void setAllowedAttributes(SingularAttribute[] allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    public void setSortAttribute(SingularAttribute attribute) {
        if (attribute == null)
            return;
        if (!Arrays.asList(allowedAttributes).contains(attribute)) {
            throw new IllegalArgumentException(
                "Sorting by attribute not allowed: " + attribute.getName()
            );
        }
        this.sortAttribute = attribute;
    }

    public boolean isMoreThanOneAvailable() {
        return getTotalRecords() != 0 && getTotalRecords() > getSize();
    }

    public boolean isAttributeDeclaredIn(SingularAttribute attribute, Bindable bindable) {
        return attribute != null && attribute.getDeclaringType().equals(bindable);
    }

    public boolean isApplicableFor(Bindable bindable) {
        return isAttributeDeclaredIn(getSortAttribute(), bindable);
    }

    public void throwIfNotApplicableFor(Path attributePath) {
        if (!isApplicableFor(attributePath.getModel())) {
            throw new IllegalArgumentException(
                "Paging settings/sort attribute are not declared " +
                    "by model of query path: " + attributePath
            );
        }
    }

    abstract public <T> TypedQuery<T> createQuery(
        EntityManager em,
        CriteriaQuery<T> criteriaQuery,
        Path attributePath
    );
}
