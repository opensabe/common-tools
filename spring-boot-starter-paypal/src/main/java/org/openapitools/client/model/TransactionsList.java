/*
 * Subscriptions
 * You can use billing plans and subscriptions to create subscriptions that process recurring PayPal payments for physical or digital goods, or services. A plan includes pricing and billing cycle information that defines the amount and frequency of charge for a subscription. You can also define a fixed plan, such as a $5 basic plan or a volume- or graduated-based plan with pricing tiers based on the quantity purchased. For more information, see <a href=\"/docs/subscriptions/\">Subscriptions Overview</a>.
 *
 * The version of the OpenAPI document: 1.6
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import org.openapitools.client.model.LinkDescription;
import org.openapitools.client.model.Transaction;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

/**
 * The list transactions for a subscription request details.
 */
@JsonPropertyOrder({
  TransactionsList.JSON_PROPERTY_TRANSACTIONS,
  TransactionsList.JSON_PROPERTY_TOTAL_ITEMS,
  TransactionsList.JSON_PROPERTY_TOTAL_PAGES,
  TransactionsList.JSON_PROPERTY_LINKS
})
@JsonTypeName("transactions_list")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-05-08T11:30:41.643502082Z[Atlantic/Reykjavik]")
public class TransactionsList {
  public static final String JSON_PROPERTY_TRANSACTIONS = "transactions";
  private List<Transaction> transactions;

  public static final String JSON_PROPERTY_TOTAL_ITEMS = "total_items";
  private Integer totalItems;

  public static final String JSON_PROPERTY_TOTAL_PAGES = "total_pages";
  private Integer totalPages;

  public static final String JSON_PROPERTY_LINKS = "links";
  private List<LinkDescription> links;

  public TransactionsList() {
  }

  @JsonCreator
  public TransactionsList(
    @JsonProperty(JSON_PROPERTY_LINKS) List<LinkDescription> links
  ) {
    this();
    this.links = links;
  }

  public TransactionsList transactions(List<Transaction> transactions) {
    
    this.transactions = transactions;
    return this;
  }

  public TransactionsList addTransactionsItem(Transaction transactionsItem) {
    if (this.transactions == null) {
      this.transactions = new ArrayList<>();
    }
    this.transactions.add(transactionsItem);
    return this;
  }

   /**
   * An array of transactions.
   * @return transactions
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TRANSACTIONS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<Transaction> getTransactions() {
    return transactions;
  }


  @JsonProperty(JSON_PROPERTY_TRANSACTIONS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTransactions(List<Transaction> transactions) {
    this.transactions = transactions;
  }


  public TransactionsList totalItems(Integer totalItems) {
    
    this.totalItems = totalItems;
    return this;
  }

   /**
   * The total number of items.
   * minimum: 0
   * maximum: 500000000
   * @return totalItems
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TOTAL_ITEMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Integer getTotalItems() {
    return totalItems;
  }


  @JsonProperty(JSON_PROPERTY_TOTAL_ITEMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTotalItems(Integer totalItems) {
    this.totalItems = totalItems;
  }


  public TransactionsList totalPages(Integer totalPages) {
    
    this.totalPages = totalPages;
    return this;
  }

   /**
   * The total number of pages.
   * minimum: 0
   * maximum: 100000000
   * @return totalPages
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TOTAL_PAGES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Integer getTotalPages() {
    return totalPages;
  }


  @JsonProperty(JSON_PROPERTY_TOTAL_PAGES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }


   /**
   * An array of request-related [HATEOAS links](/docs/api/reference/api-responses/#hateoas-links).
   * @return links
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_LINKS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<LinkDescription> getLinks() {
    return links;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionsList transactionsList = (TransactionsList) o;
    return Objects.equals(this.transactions, transactionsList.transactions) &&
        Objects.equals(this.totalItems, transactionsList.totalItems) &&
        Objects.equals(this.totalPages, transactionsList.totalPages) &&
        Objects.equals(this.links, transactionsList.links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactions, totalItems, totalPages, links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionsList {\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
    sb.append("    totalItems: ").append(toIndentedString(totalItems)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `transactions` to the URL query string
    if (getTransactions() != null) {
      for (int i = 0; i < getTransactions().size(); i++) {
        if (getTransactions().get(i) != null) {
          joiner.add(getTransactions().get(i).toUrlQueryString(String.format("%stransactions%s%s", prefix, suffix,
              "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `total_items` to the URL query string
    if (getTotalItems() != null) {
      try {
        joiner.add(String.format("%stotal_items%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getTotalItems()), "UTF-8").replaceAll("\\+", "%20")));
      } catch (UnsupportedEncodingException e) {
        // Should never happen, UTF-8 is always supported
        throw new RuntimeException(e);
      }
    }

    // add `total_pages` to the URL query string
    if (getTotalPages() != null) {
      try {
        joiner.add(String.format("%stotal_pages%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getTotalPages()), "UTF-8").replaceAll("\\+", "%20")));
      } catch (UnsupportedEncodingException e) {
        // Should never happen, UTF-8 is always supported
        throw new RuntimeException(e);
      }
    }

    // add `links` to the URL query string
    if (getLinks() != null) {
      for (int i = 0; i < getLinks().size(); i++) {
        if (getLinks().get(i) != null) {
          joiner.add(getLinks().get(i).toUrlQueryString(String.format("%slinks%s%s", prefix, suffix,
              "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    return joiner.toString();
  }

}

