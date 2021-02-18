package restsvc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Beer implements Serializable{
  
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @ApiModelProperty(value = "Name of Beer")
 /**
   * Name of Beer
  **/
  private String name = null;

  @ApiModelProperty(value = "Origin country of Beer")
 /**
   * Origin country of Beer
  **/
  private String country = null;

  @ApiModelProperty(value = "Type of Beer")
 /**
   * Type of Beer
  **/
  private String type = null;

  @ApiModelProperty(value = "Rating from customers")
  @Valid
 /**
   * Rating from customers
  **/
  private BigDecimal rating = null;

  @ApiModelProperty(value = "Stock status")
 /**
   * Stock status
  **/
  private String status = null;
 /**
   * Name of Beer
   * @return name
  **/
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Beer name(String name) {
    this.name = name;
    return this;
  }

 /**
   * Origin country of Beer
   * @return country
  **/
  @JsonProperty("country")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Beer country(String country) {
    this.country = country;
    return this;
  }

 /**
   * Type of Beer
   * @return type
  **/
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Beer type(String type) {
    this.type = type;
    return this;
  }

 /**
   * Rating from customers
   * @return rating
  **/
  @JsonProperty("rating")
  public BigDecimal getRating() {
    return rating;
  }

  public void setRating(BigDecimal rating) {
    this.rating = rating;
  }

  public Beer rating(BigDecimal rating) {
    this.rating = rating;
    return this;
  }

 /**
   * Stock status
   * @return status
  **/
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Beer status(String status) {
    this.status = status;
    return this;
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Beer {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private static String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public Beer(String name, String country, String type, @Valid BigDecimal rating, String status) {
    this.name = name;
    this.country = country;
    this.type = type;
    this.rating = rating;
    this.status = status;
  }
}

