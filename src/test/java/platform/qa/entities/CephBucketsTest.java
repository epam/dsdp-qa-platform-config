/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package platform.qa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CephBuckets Tests")
public class CephBucketsTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create CephBuckets with no-args constructor")
    void shouldCreateCephBucketsWithNoArgsConstructor() {
      CephBuckets cephBuckets = new CephBuckets();
      
      assertThat(cephBuckets).isNotNull();
      assertThat(cephBuckets.getSignatureBucket()).isNull();
      assertThat(cephBuckets.getDataFileBucket()).isNull();
      assertThat(cephBuckets.getLowCodeFileBucket()).isNull();
      assertThat(cephBuckets.getExcerptBucket()).isNull();
      assertThat(cephBuckets.getDsoCertBucket()).isNull();
    }

    @Test
    @DisplayName("Should create CephBuckets with all-args constructor")
    void shouldCreateCephBucketsWithAllArgsConstructor() {
      String signatureBucket = "signature-bucket";
      String dataFileBucket = "data-file-bucket";
      String lowCodeFileBucket = "low-code-file-bucket";
      String excerptBucket = "excerpt-bucket";
      String dsoCertBucket = "dso-cert-bucket";
      
      CephBuckets cephBuckets = new CephBuckets(
          signatureBucket, dataFileBucket, lowCodeFileBucket, excerptBucket, dsoCertBucket);
      
      assertThat(cephBuckets).isNotNull();
      assertThat(cephBuckets.getSignatureBucket()).isEqualTo(signatureBucket);
      assertThat(cephBuckets.getDataFileBucket()).isEqualTo(dataFileBucket);
      assertThat(cephBuckets.getLowCodeFileBucket()).isEqualTo(lowCodeFileBucket);
      assertThat(cephBuckets.getExcerptBucket()).isEqualTo(excerptBucket);
      assertThat(cephBuckets.getDsoCertBucket()).isEqualTo(dsoCertBucket);
    }
  }

  @Nested
  @DisplayName("Getter and Setter Tests")
  class GetterSetterTests {

    @Test
    @DisplayName("Should set and get signatureBucket")
    void shouldSetAndGetSignatureBucket() {
      CephBuckets cephBuckets = new CephBuckets();
      String expectedValue = "test-signature-bucket";
      
      cephBuckets.setSignatureBucket(expectedValue);
      
      assertThat(cephBuckets.getSignatureBucket()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should set and get dataFileBucket")
    void shouldSetAndGetDataFileBucket() {
      CephBuckets cephBuckets = new CephBuckets();
      String expectedValue = "test-data-file-bucket";
      
      cephBuckets.setDataFileBucket(expectedValue);
      
      assertThat(cephBuckets.getDataFileBucket()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should set and get lowCodeFileBucket")
    void shouldSetAndGetLowCodeFileBucket() {
      CephBuckets cephBuckets = new CephBuckets();
      String expectedValue = "test-low-code-file-bucket";
      
      cephBuckets.setLowCodeFileBucket(expectedValue);
      
      assertThat(cephBuckets.getLowCodeFileBucket()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should set and get excerptBucket")
    void shouldSetAndGetExcerptBucket() {
      CephBuckets cephBuckets = new CephBuckets();
      String expectedValue = "test-excerpt-bucket";
      
      cephBuckets.setExcerptBucket(expectedValue);
      
      assertThat(cephBuckets.getExcerptBucket()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should set and get dsoCertBucket")
    void shouldSetAndGetDsoCertBucket() {
      CephBuckets cephBuckets = new CephBuckets();
      String expectedValue = "test-dso-cert-bucket";
      
      cephBuckets.setDsoCertBucket(expectedValue);
      
      assertThat(cephBuckets.getDsoCertBucket()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
      CephBuckets cephBuckets = new CephBuckets();
      
      cephBuckets.setSignatureBucket(null);
      cephBuckets.setDataFileBucket(null);
      cephBuckets.setLowCodeFileBucket(null);
      cephBuckets.setExcerptBucket(null);
      cephBuckets.setDsoCertBucket(null);
      
      assertThat(cephBuckets.getSignatureBucket()).isNull();
      assertThat(cephBuckets.getDataFileBucket()).isNull();
      assertThat(cephBuckets.getLowCodeFileBucket()).isNull();
      assertThat(cephBuckets.getExcerptBucket()).isNull();
      assertThat(cephBuckets.getDsoCertBucket()).isNull();
    }

    @Test
    @DisplayName("Should handle empty strings")
    void shouldHandleEmptyStrings() {
      CephBuckets cephBuckets = new CephBuckets();
      String emptyString = "";
      
      cephBuckets.setSignatureBucket(emptyString);
      cephBuckets.setDataFileBucket(emptyString);
      cephBuckets.setLowCodeFileBucket(emptyString);
      cephBuckets.setExcerptBucket(emptyString);
      cephBuckets.setDsoCertBucket(emptyString);
      
      assertThat(cephBuckets.getSignatureBucket()).isEqualTo(emptyString);
      assertThat(cephBuckets.getDataFileBucket()).isEqualTo(emptyString);
      assertThat(cephBuckets.getLowCodeFileBucket()).isEqualTo(emptyString);
      assertThat(cephBuckets.getExcerptBucket()).isEqualTo(emptyString);
      assertThat(cephBuckets.getDsoCertBucket()).isEqualTo(emptyString);
    }
  }

  @Nested
  @DisplayName("Lombok Generated Methods Tests")
  class LombokGeneratedMethodsTests {

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
      CephBuckets cephBuckets1 = new CephBuckets("sig1", "data1", "low1", "exc1", "dso1");
      CephBuckets cephBuckets2 = new CephBuckets("sig1", "data1", "low1", "exc1", "dso1");
      CephBuckets cephBuckets3 = new CephBuckets("sig2", "data2", "low2", "exc2", "dso2");
      
      assertThat(cephBuckets1).isEqualTo(cephBuckets2);
      assertThat(cephBuckets1).isNotEqualTo(cephBuckets3);
      assertThat(cephBuckets1).isEqualTo(cephBuckets1); // reflexive
      assertThat(cephBuckets1).isNotEqualTo(null);
      assertThat(cephBuckets1).isNotEqualTo("not a CephBuckets object");
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
      CephBuckets cephBuckets1 = new CephBuckets("sig1", "data1", "low1", "exc1", "dso1");
      CephBuckets cephBuckets2 = new CephBuckets("sig1", "data1", "low1", "exc1", "dso1");
      CephBuckets cephBuckets3 = new CephBuckets("sig2", "data2", "low2", "exc2", "dso2");
      
      assertThat(cephBuckets1.hashCode()).isEqualTo(cephBuckets2.hashCode());
      assertThat(cephBuckets1.hashCode()).isNotEqualTo(cephBuckets3.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
      CephBuckets cephBuckets = new CephBuckets("sig", "data", "low", "exc", "dso");
      
      String toString = cephBuckets.toString();
      
      assertThat(toString).contains("CephBuckets");
      assertThat(toString).contains("signatureBucket=sig");
      assertThat(toString).contains("dataFileBucket=data");
      assertThat(toString).contains("lowCodeFileBucket=low");
      assertThat(toString).contains("excerptBucket=exc");
      assertThat(toString).contains("dsoCertBucket=dso");
    }

    @Test
    @DisplayName("Should handle toString with null values")
    void shouldHandleToStringWithNullValues() {
      CephBuckets cephBuckets = new CephBuckets();
      
      String toString = cephBuckets.toString();
      
      assertThat(toString).contains("CephBuckets");
      assertThat(toString).contains("signatureBucket=null");
      assertThat(toString).contains("dataFileBucket=null");
      assertThat(toString).contains("lowCodeFileBucket=null");
      assertThat(toString).contains("excerptBucket=null");
      assertThat(toString).contains("dsoCertBucket=null");
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work with typical bucket names")
    void shouldWorkWithTypicalBucketNames() {
      CephBuckets cephBuckets = new CephBuckets();
      
      cephBuckets.setSignatureBucket("platform-signature-bucket");
      cephBuckets.setDataFileBucket("platform-data-files");
      cephBuckets.setLowCodeFileBucket("platform-lowcode-files");
      cephBuckets.setExcerptBucket("platform-excerpts");
      cephBuckets.setDsoCertBucket("platform-dso-certificates");
      
      assertThat(cephBuckets.getSignatureBucket()).isEqualTo("platform-signature-bucket");
      assertThat(cephBuckets.getDataFileBucket()).isEqualTo("platform-data-files");
      assertThat(cephBuckets.getLowCodeFileBucket()).isEqualTo("platform-lowcode-files");
      assertThat(cephBuckets.getExcerptBucket()).isEqualTo("platform-excerpts");
      assertThat(cephBuckets.getDsoCertBucket()).isEqualTo("platform-dso-certificates");
    }

    @Test
    @DisplayName("Should support fluent-style usage")
    void shouldSupportFluentStyleUsage() {
      CephBuckets cephBuckets = new CephBuckets();
      
      // Test that setters return void (typical for Lombok @Data)
      cephBuckets.setSignatureBucket("test1");
      cephBuckets.setDataFileBucket("test2");
      cephBuckets.setLowCodeFileBucket("test3");
      cephBuckets.setExcerptBucket("test4");
      cephBuckets.setDsoCertBucket("test5");
      
      assertThat(cephBuckets.getSignatureBucket()).isEqualTo("test1");
      assertThat(cephBuckets.getDataFileBucket()).isEqualTo("test2");
      assertThat(cephBuckets.getLowCodeFileBucket()).isEqualTo("test3");
      assertThat(cephBuckets.getExcerptBucket()).isEqualTo("test4");
      assertThat(cephBuckets.getDsoCertBucket()).isEqualTo("test5");
    }

    @Test
    @DisplayName("Should handle mixed null and non-null values")
    void shouldHandleMixedNullAndNonNullValues() {
      CephBuckets cephBuckets = new CephBuckets();
      
      cephBuckets.setSignatureBucket("signature-bucket");
      cephBuckets.setDataFileBucket(null);
      cephBuckets.setLowCodeFileBucket("lowcode-bucket");
      cephBuckets.setExcerptBucket(null);
      cephBuckets.setDsoCertBucket("dso-bucket");
      
      assertThat(cephBuckets.getSignatureBucket()).isEqualTo("signature-bucket");
      assertThat(cephBuckets.getDataFileBucket()).isNull();
      assertThat(cephBuckets.getLowCodeFileBucket()).isEqualTo("lowcode-bucket");
      assertThat(cephBuckets.getExcerptBucket()).isNull();
      assertThat(cephBuckets.getDsoCertBucket()).isEqualTo("dso-bucket");
    }

    @Test
    @DisplayName("Should create multiple independent instances")
    void shouldCreateMultipleIndependentInstances() {
      CephBuckets buckets1 = new CephBuckets("sig1", "data1", "low1", "exc1", "dso1");
      CephBuckets buckets2 = new CephBuckets("sig2", "data2", "low2", "exc2", "dso2");
      
      // Modify one instance
      buckets1.setSignatureBucket("modified-sig1");
      
      // Verify the other instance is not affected
      assertThat(buckets1.getSignatureBucket()).isEqualTo("modified-sig1");
      assertThat(buckets2.getSignatureBucket()).isEqualTo("sig2");
      
      // Verify they are different objects
      assertThat(buckets1).isNotSameAs(buckets2);
      assertThat(buckets1).isNotEqualTo(buckets2);
    }
  }
}
