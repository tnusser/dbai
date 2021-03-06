//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.26 at 04:09:17 PM CET 
//


package minibase.catalog.v1;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for table complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="table"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cardinality" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="pages" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="width" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="columns"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="column" type="{http://minibase/catalog/v1}column" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="primaryKey" type="{http://minibase/catalog/v1}primaryKey" minOccurs="0"/&gt;
 *         &lt;element name="foreignKey" type="{http://minibase/catalog/v1}foreignKey" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="indexes" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="index" type="{http://minibase/catalog/v1}index" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "table", propOrder = {
        "cardinality",
        "pages",
        "width",
        "columns",
        "primaryKey",
        "foreignKey",
        "indexes"
})
public class Table {

    protected int cardinality;
    protected int pages;
    protected double width;
    @XmlElement(required = true)
    protected Table.Columns columns;
    protected PrimaryKey primaryKey;
    protected List<ForeignKey> foreignKey;
    protected Table.Indexes indexes;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the cardinality property.
     */
    public int getCardinality() {
        return cardinality;
    }

    /**
     * Sets the value of the cardinality property.
     */
    public void setCardinality(int value) {
        this.cardinality = value;
    }

    /**
     * Gets the value of the pages property.
     */
    public int getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     */
    public void setPages(int value) {
        this.pages = value;
    }

    /**
     * Gets the value of the width property.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     */
    public void setWidth(double value) {
        this.width = value;
    }

    /**
     * Gets the value of the columns property.
     *
     * @return possible object is
     * {@link Table.Columns }
     */
    public Table.Columns getColumns() {
        return columns;
    }

    /**
     * Sets the value of the columns property.
     *
     * @param value allowed object is
     *              {@link Table.Columns }
     */
    public void setColumns(Table.Columns value) {
        this.columns = value;
    }

    /**
     * Gets the value of the primaryKey property.
     *
     * @return possible object is
     * {@link PrimaryKey }
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the value of the primaryKey property.
     *
     * @param value allowed object is
     *              {@link PrimaryKey }
     */
    public void setPrimaryKey(PrimaryKey value) {
        this.primaryKey = value;
    }

    /**
     * Gets the value of the foreignKey property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the foreignKey property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForeignKey().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ForeignKey }
     */
    public List<ForeignKey> getForeignKey() {
        if (foreignKey == null) {
            foreignKey = new ArrayList<ForeignKey>();
        }
        return this.foreignKey;
    }

    /**
     * Gets the value of the indexes property.
     *
     * @return possible object is
     * {@link Table.Indexes }
     */
    public Table.Indexes getIndexes() {
        return indexes;
    }

    /**
     * Sets the value of the indexes property.
     *
     * @param value allowed object is
     *              {@link Table.Indexes }
     */
    public void setIndexes(Table.Indexes value) {
        this.indexes = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="column" type="{http://minibase/catalog/v1}column" maxOccurs="unbounded"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "column"
    })
    public static class Columns {

        @XmlElement(required = true)
        protected List<Column> column;

        /**
         * Gets the value of the column property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the column property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getColumn().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Column }
         */
        public List<Column> getColumn() {
            if (column == null) {
                column = new ArrayList<Column>();
            }
            return this.column;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="index" type="{http://minibase/catalog/v1}index" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "index"
    })
    public static class Indexes {

        protected List<Index> index;

        /**
         * Gets the value of the index property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the index property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getIndex().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Index }
         */
        public List<Index> getIndex() {
            if (index == null) {
                index = new ArrayList<Index>();
            }
            return this.index;
        }

    }

}
