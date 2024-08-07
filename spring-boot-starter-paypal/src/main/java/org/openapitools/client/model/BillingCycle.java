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
import org.openapitools.client.model.Frequency;
import org.openapitools.client.model.PricingScheme;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringJoiner;

/**
 * The billing cycle details.
 */
@JsonPropertyOrder({
  BillingCycle.JSON_PROPERTY_PRICING_SCHEME,
  BillingCycle.JSON_PROPERTY_FREQUENCY,
  BillingCycle.JSON_PROPERTY_TENURE_TYPE,
  BillingCycle.JSON_PROPERTY_SEQUENCE,
  BillingCycle.JSON_PROPERTY_TOTAL_CYCLES
})
@JsonTypeName("billing_cycle")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-05-08T11:30:41.643502082Z[Atlantic/Reykjavik]")
public class BillingCycle {
  public static final String JSON_PROPERTY_PRICING_SCHEME = "pricing_scheme";
  private PricingScheme pricingScheme;

  public static final String JSON_PROPERTY_FREQUENCY = "frequency";
  private Frequency frequency;

  /**
   * The tenure type of the billing cycle. In case of a plan having trial cycle, only 2 trial cycles are allowed per plan.
   */
  public enum TenureTypeEnum {
    REGULAR("REGULAR"),
    
    TRIAL("TRIAL");

    private String value;

    TenureTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TenureTypeEnum fromValue(String value) {
      for (TenureTypeEnum b : TenureTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_TENURE_TYPE = "tenure_type";
  private TenureTypeEnum tenureType;

  public static final String JSON_PROPERTY_SEQUENCE = "sequence";
  private Integer sequence;

  public static final String JSON_PROPERTY_TOTAL_CYCLES = "total_cycles";
  private Integer totalCycles = 1;

  public BillingCycle() {
  }

  public BillingCycle pricingScheme(PricingScheme pricingScheme) {
    
    this.pricingScheme = pricingScheme;
    return this;
  }

   /**
   * Get pricingScheme
   * @return pricingScheme
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PRICING_SCHEME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public PricingScheme getPricingScheme() {
    return pricingScheme;
  }


  @JsonProperty(JSON_PROPERTY_PRICING_SCHEME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPricingScheme(PricingScheme pricingScheme) {
    this.pricingScheme = pricingScheme;
  }


  public BillingCycle frequency(Frequency frequency) {
    
    this.frequency = frequency;
    return this;
  }

   /**
   * Get frequency
   * @return frequency
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_FREQUENCY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Frequency getFrequency() {
    return frequency;
  }


  @JsonProperty(JSON_PROPERTY_FREQUENCY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFrequency(Frequency frequency) {
    this.frequency = frequency;
  }


  public BillingCycle tenureType(TenureTypeEnum tenureType) {
    
    this.tenureType = tenureType;
    return this;
  }

   /**
   * The tenure type of the billing cycle. In case of a plan having trial cycle, only 2 trial cycles are allowed per plan.
   * @return tenureType
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TENURE_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public TenureTypeEnum getTenureType() {
    return tenureType;
  }


  @JsonProperty(JSON_PROPERTY_TENURE_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTenureType(TenureTypeEnum tenureType) {
    this.tenureType = tenureType;
  }


  public BillingCycle sequence(Integer sequence) {
    
    this.sequence = sequence;
    return this;
  }

   /**
   * The order in which this cycle is to run among other billing cycles. For example, a trial billing cycle has a &#x60;sequence&#x60; of &#x60;1&#x60; while a regular billing cycle has a &#x60;sequence&#x60; of &#x60;2&#x60;, so that trial cycle runs before the regular cycle.
   * minimum: 1
   * maximum: 99
   * @return sequence
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SEQUENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Integer getSequence() {
    return sequence;
  }


  @JsonProperty(JSON_PROPERTY_SEQUENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSequence(Integer sequence) {
    this.sequence = sequence;
  }


  public BillingCycle totalCycles(Integer totalCycles) {
    
    this.totalCycles = totalCycles;
    return this;
  }

   /**
   * The number of times this billing cycle gets executed. Trial billing cycles can only be executed a finite number of times (value between &lt;code&gt;1&lt;/code&gt; and &lt;code&gt;999&lt;/code&gt; for &lt;code&gt;total_cycles&lt;/code&gt;). Regular billing cycles can be executed infinite times (value of &lt;code&gt;0&lt;/code&gt; for &lt;code&gt;total_cycles&lt;/code&gt;) or a finite number of times (value between &lt;code&gt;1&lt;/code&gt; and &lt;code&gt;999&lt;/code&gt; for &lt;code&gt;total_cycles&lt;/code&gt;).
   * minimum: 0
   * maximum: 999
   * @return totalCycles
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TOTAL_CYCLES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Integer getTotalCycles() {
    return totalCycles;
  }


  @JsonProperty(JSON_PROPERTY_TOTAL_CYCLES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTotalCycles(Integer totalCycles) {
    this.totalCycles = totalCycles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BillingCycle billingCycle = (BillingCycle) o;
    return Objects.equals(this.pricingScheme, billingCycle.pricingScheme) &&
        Objects.equals(this.frequency, billingCycle.frequency) &&
        Objects.equals(this.tenureType, billingCycle.tenureType) &&
        Objects.equals(this.sequence, billingCycle.sequence) &&
        Objects.equals(this.totalCycles, billingCycle.totalCycles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pricingScheme, frequency, tenureType, sequence, totalCycles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BillingCycle {\n");
    sb.append("    pricingScheme: ").append(toIndentedString(pricingScheme)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    tenureType: ").append(toIndentedString(tenureType)).append("\n");
    sb.append("    sequence: ").append(toIndentedString(sequence)).append("\n");
    sb.append("    totalCycles: ").append(toIndentedString(totalCycles)).append("\n");
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

    // add `pricing_scheme` to the URL query string
    if (getPricingScheme() != null) {
      joiner.add(getPricingScheme().toUrlQueryString(prefix + "pricing_scheme" + suffix));
    }

    // add `frequency` to the URL query string
    if (getFrequency() != null) {
      joiner.add(getFrequency().toUrlQueryString(prefix + "frequency" + suffix));
    }

    // add `tenure_type` to the URL query string
    if (getTenureType() != null) {
      try {
        joiner.add(String.format("%stenure_type%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getTenureType()), "UTF-8").replaceAll("\\+", "%20")));
      } catch (UnsupportedEncodingException e) {
        // Should never happen, UTF-8 is always supported
        throw new RuntimeException(e);
      }
    }

    // add `sequence` to the URL query string
    if (getSequence() != null) {
      try {
        joiner.add(String.format("%ssequence%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSequence()), "UTF-8").replaceAll("\\+", "%20")));
      } catch (UnsupportedEncodingException e) {
        // Should never happen, UTF-8 is always supported
        throw new RuntimeException(e);
      }
    }

    // add `total_cycles` to the URL query string
    if (getTotalCycles() != null) {
      try {
        joiner.add(String.format("%stotal_cycles%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getTotalCycles()), "UTF-8").replaceAll("\\+", "%20")));
      } catch (UnsupportedEncodingException e) {
        // Should never happen, UTF-8 is always supported
        throw new RuntimeException(e);
      }
    }

    return joiner.toString();
  }

}

