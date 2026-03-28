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

package platform.qa.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import platform.qa.exceptions.LocalizationExceptions;

@DisplayName("LocalizationUtils Tests")
class LocalizationUtilsTest {

  @TempDir
  Path tempDir;
  private LocalizationUtils localizationUtils;
  private String testJsonFilePath;

  @BeforeEach
  void setUp() throws IOException {
    localizationUtils = new LocalizationUtils();
    
    // Create a test JSON file with localization data
    String testJsonContent = """
        {
          "en": {
            "greeting": "Hello",
            "farewell": "Goodbye",
            "nested": {
              "level1": {
                "message": "Deep message"
              }
            }
          },
          "uk": {
            "greeting": "Привіт",
            "farewell": "До побачення",
            "nested": {
              "level1": {
                "message": "Глибоке повідомлення"
              }
            }
          },
          "fr": {
            "greeting": "Bonjour",
            "farewell": "Au revoir"
          }
        }
        """;
    
    Path testFile = tempDir.resolve("test-localization.json");
    Files.write(testFile, testJsonContent.getBytes());
    testJsonFilePath = testFile.toString();
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create LocalizationUtils instance")
    void shouldCreateLocalizationUtilsInstance() {
      LocalizationUtils utils = new LocalizationUtils();
      assertThat(utils).isNotNull();
    }
  }

  @Nested
  @DisplayName("Upload Localization File Tests")
  class UploadLocalizationFileTests {

    @Test
    @DisplayName("Should upload valid JSON file")
    void shouldUploadValidJsonFile() {
      JsonNode result = localizationUtils.uploadLocalizationFile(testJsonFilePath);
      
      assertThat(result).isNotNull();
      assertThat(result.has("en")).isTrue();
      assertThat(result.has("uk")).isTrue();
      assertThat(result.has("fr")).isTrue();
    }

    @Test
    @DisplayName("Should throw LocalizationFileLoadException for non-existent file")
    void shouldThrowLocalizationFileLoadExceptionForNonExistentFile() {
      String nonExistentPath = tempDir.resolve("non-existent.json").toString();
      
      assertThatThrownBy(() -> localizationUtils.uploadLocalizationFile(nonExistentPath))
          .isInstanceOf(LocalizationExceptions.LocalizationFileLoadException.class)
          .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should throw LocalizationFileLoadException for invalid JSON")
    void shouldThrowLocalizationFileLoadExceptionForInvalidJson() throws IOException {
      // Create invalid JSON file
      Path invalidJsonFile = tempDir.resolve("invalid.json");
      Files.write(invalidJsonFile, "{ invalid json content".getBytes());
      
      assertThatThrownBy(() -> localizationUtils.uploadLocalizationFile(invalidJsonFile.toString()))
          .isInstanceOf(LocalizationExceptions.LocalizationFileLoadException.class)
          .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should handle empty JSON file")
    void shouldHandleEmptyJsonFile() throws IOException {
      // Create empty JSON file
      Path emptyJsonFile = tempDir.resolve("empty.json");
      Files.write(emptyJsonFile, "{}".getBytes());
      
      JsonNode result = localizationUtils.uploadLocalizationFile(emptyJsonFile.toString());
      
      assertThat(result).isNotNull();
      assertThat(result.isEmpty()).isTrue();
    }
  }

  @Nested
  @DisplayName("Get Localization Data By Key Tests")
  class GetLocalizationDataByKeyTests {

    @Test
    @DisplayName("Should get localization data for English")
    void shouldGetLocalizationDataForEnglish() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "en", "greeting");
      
      assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("Should get localization data for Ukrainian")
    void shouldGetLocalizationDataForUkrainian() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "uk", "greeting");
      
      assertThat(result).isEqualTo("Привіт");
    }

    @Test
    @DisplayName("Should get different keys for same language")
    void shouldGetDifferentKeysForSameLanguage() {
      String greeting = localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "en", "greeting");
      String farewell = localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "en", "farewell");
      
      assertThat(greeting).isEqualTo("Hello");
      assertThat(farewell).isEqualTo("Goodbye");
    }

    @Test
    @DisplayName("Should handle missing key")
    void shouldHandleMissingKey() {
      assertThatThrownBy(() -> localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "en", "nonexistent"))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle missing language")
    void shouldHandleMissingLanguage() {
      assertThatThrownBy(() -> localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "de", "greeting"))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("Get Localization Data By Path Tests")
  class GetLocalizationDataByPathTests {

    @Test
    @DisplayName("Should get nested localization data")
    void shouldGetNestedLocalizationData() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "en", "message", "nested", "level1");
      
      assertThat(result).isEqualTo("Deep message");
    }

    @Test
    @DisplayName("Should get nested data for different language")
    void shouldGetNestedDataForDifferentLanguage() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "uk", "message", "nested", "level1");
      
      assertThat(result).isEqualTo("Глибоке повідомлення");
    }

    @Test
    @DisplayName("Should handle single path segment")
    void shouldHandleSinglePathSegment() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "en", "greeting");
      
      assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("Should handle empty path segments")
    void shouldHandleEmptyPathSegments() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "en", "greeting", new String[0]);
      
      assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("Should handle missing nested path")
    void shouldHandleMissingNestedPath() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "fr", "message", "nested", "level1");
      
      // Should return empty string for missing path
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle invalid path segments")
    void shouldHandleInvalidPathSegments() {
      String result = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "en", "nonexistent", "invalid", "path");
      
      // Should return empty string for invalid path
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Get Localization Data For All Languages Tests")
  class GetLocalizationDataForAllLanguagesTests {

    @Test
    @DisplayName("Should get greeting for all languages")
    void shouldGetGreetingForAllLanguages() {
      List<String> results = localizationUtils.getLocalizationDataForAllLanguages(
          testJsonFilePath, "greeting");
      
      assertThat(results).hasSize(3).contains("\"Hello\"", "\"Привіт\"", "\"Bonjour\"");
    }

    @Test
    @DisplayName("Should get farewell for all languages")
    void shouldGetFarewellForAllLanguages() {
      List<String> results = localizationUtils.getLocalizationDataForAllLanguages(
          testJsonFilePath, "farewell");
      
      assertThat(results).hasSize(3).contains("\"Goodbye\"", "\"До побачення\"", "\"Au revoir\"");
    }

    @Test
    @DisplayName("Should handle missing key in some languages")
    void shouldHandleMissingKeyInSomeLanguages() {
      List<String> results = localizationUtils.getLocalizationDataForAllLanguages(
          testJsonFilePath, "message");
      
      // Only en and uk have nested.level1.message
      assertThat(results).hasSize(2).contains("\"Deep message\"", "\"Глибоке повідомлення\"");
    }

    @Test
    @DisplayName("Should return empty list for non-existent key")
    void shouldReturnEmptyListForNonExistentKey() {
      List<String> results = localizationUtils.getLocalizationDataForAllLanguages(
          testJsonFilePath, "nonexistent");
      
      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should handle complex nested values")
    void shouldHandleComplexNestedValues() throws IOException {
      // Create JSON with complex nested structure
      String complexJsonContent = """
          {
            "en": {
              "complex": {
                "array": ["item1", "item2"],
                "object": {"key": "value"}
              }
            },
            "uk": {
              "complex": {
                "array": ["елемент1", "елемент2"],
                "object": {"key": "значення"}
              }
            }
          }
          """;
      
      Path complexFile = tempDir.resolve("complex.json");
      Files.write(complexFile, complexJsonContent.getBytes());
      
      List<String> results = localizationUtils.getLocalizationDataForAllLanguages(
          complexFile.toString(), "array");
      
      assertThat(results).hasSize(2);
      assertThat(results.get(0)).contains("item1", "item2");
      assertThat(results.get(1)).contains("елемент1", "елемент2");
    }
  }

  @Nested
  @DisplayName("Get Localization Data From Flat JSON Tests")
  class GetLocalizationDataFromFlatJsonTests {

    private String flatJsonFilePath;

    @BeforeEach
    void setUpFlatJson() throws IOException {
      // Create a flat structure JSON file
      String flatJsonContent = """
          {
            "en": {
              "Name": "Name",
              "Surname": "Surname",
              "Email": "Email",
              "Phone": "Phone Number"
            },
            "az": {
              "Name": "Ad",
              "Surname": "Soyad",
              "Email": "Elektron poçt",
              "Phone": "Telefon nömrəsi"
            },
            "uk": {
              "Name": "Ім'я",
              "Surname": "Прізвище",
              "Email": "Електронна пошта",
              "Phone": "Номер телефону"
            }
          }
          """;
      
      Path flatJsonFile = tempDir.resolve("flat-localization.json");
      Files.write(flatJsonFile, flatJsonContent.getBytes());
      flatJsonFilePath = flatJsonFile.toString();
    }

    @Test
    @DisplayName("Should get localized value from flat JSON for English")
    void shouldGetLocalizedValueFromFlatJsonForEnglish() {
      LocalizationUtils utils = new LocalizationUtils();
      
      String result = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "Name");
      
      assertThat(result).isEqualTo("Name");
    }

    @Test
    @DisplayName("Should get localized value from flat JSON for Azerbaijani")
    void shouldGetLocalizedValueFromFlatJsonForAzerbaijani() {
      LocalizationUtils utils = new LocalizationUtils();
      
      String result = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "az", "Name");
      
      assertThat(result).isEqualTo("Ad");
    }

    @Test
    @DisplayName("Should get localized value from flat JSON for Ukrainian")
    void shouldGetLocalizedValueFromFlatJsonForUkrainian() {
      LocalizationUtils utils = new LocalizationUtils();
      
      String result = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "uk", "Email");
      
      assertThat(result).isEqualTo("Електронна пошта");
    }

    @Test
    @DisplayName("Should get different keys for same locale from flat JSON")
    void shouldGetDifferentKeysForSameLocaleFromFlatJson() {
      LocalizationUtils utils = new LocalizationUtils();
      
      String name = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "Name");
      String surname = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "Surname");
      String email = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "Email");
      
      assertThat(name).isEqualTo("Name");
      assertThat(surname).isEqualTo("Surname");
      assertThat(email).isEqualTo("Email");
    }

    @Test
    @DisplayName("Should cache localization data after first load")
    void shouldCacheLocalizationDataAfterFirstLoad() {
      LocalizationUtils utils = new LocalizationUtils();
      
      // First call loads the data
      String firstResult = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "Name");
      
      // Second call should use cached data
      String secondResult = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "Surname");
      
      assertThat(firstResult).isEqualTo("Name");
      assertThat(secondResult).isEqualTo("Surname");
    }

    @Test
    @DisplayName("Should throw LocaleNotFoundException when locale not found")
    void shouldThrowLocaleNotFoundExceptionWhenLocaleNotFound() {
      LocalizationUtils utils = new LocalizationUtils();
      
      assertThatThrownBy(() -> utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "de", "Name"))
          .isInstanceOf(LocalizationExceptions.LocaleNotFoundException.class)
          .hasMessage("Locale not found: de");
    }

    @Test
    @DisplayName("Should throw LocalizationKeyNotFoundException when key not found")
    void shouldThrowLocalizationKeyNotFoundExceptionWhenKeyNotFound() {
      LocalizationUtils utils = new LocalizationUtils();
      
      assertThatThrownBy(() -> utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          flatJsonFilePath, "en", "NonExistentKey"))
          .isInstanceOf(LocalizationExceptions.LocalizationKeyNotFoundException.class)
          .hasMessage("Key not found: NonExistentKey for locale: en");
    }

    @Test
    @DisplayName("Should throw LocalizationFileLoadException when file not found")
    void shouldThrowLocalizationFileLoadExceptionWhenFileNotFound() {
      LocalizationUtils utils = new LocalizationUtils();
      String nonExistentPath = tempDir.resolve("non-existent-flat.json").toString();
      
      assertThatThrownBy(() -> utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          nonExistentPath, "en", "Name"))
          .isInstanceOf(LocalizationExceptions.LocalizationFileLoadException.class)
          .hasMessage("Failed to load localization file")
          .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should throw LocalizationFileLoadException when JSON is invalid")
    void shouldThrowLocalizationFileLoadExceptionWhenJsonIsInvalid() throws IOException {
      LocalizationUtils utils = new LocalizationUtils();
      
      // Create invalid JSON file
      Path invalidJsonFile = tempDir.resolve("invalid-flat.json");
      Files.write(invalidJsonFile, "{ invalid json".getBytes());
      
      assertThatThrownBy(() -> utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          invalidJsonFile.toString(), "en", "Name"))
          .isInstanceOf(LocalizationExceptions.LocalizationFileLoadException.class)
          .hasMessage("Failed to load localization file")
          .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should handle empty locale object")
    void shouldHandleEmptyLocaleObject() throws IOException {
      LocalizationUtils utils = new LocalizationUtils();
      
      // Create JSON with empty locale
      String emptyLocaleJson = """
          {
            "en": {},
            "az": {
              "Name": "Ad"
            }
          }
          """;
      
      Path emptyLocaleFile = tempDir.resolve("empty-locale.json");
      Files.write(emptyLocaleFile, emptyLocaleJson.getBytes());
      
      assertThatThrownBy(() -> utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          emptyLocaleFile.toString(), "en", "Name"))
          .isInstanceOf(LocalizationExceptions.LocalizationKeyNotFoundException.class)
          .hasMessage("Key not found: Name for locale: en");
    }

    @Test
    @DisplayName("Should handle special characters in values")
    void shouldHandleSpecialCharactersInValues() throws IOException {
      LocalizationUtils utils = new LocalizationUtils();
      
      String specialCharsJson = """
          {
            "en": {
              "SpecialChars": "Value with 'apostrophes' & symbols < > @"
            },
            "az": {
              "SpecialChars": "Dəyər 'apostrof' və simvollar ilə"
            }
          }
          """;
      
      Path specialCharsFile = tempDir.resolve("special-chars.json");
      Files.write(specialCharsFile, specialCharsJson.getBytes());
      
      String result = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          specialCharsFile.toString(), "en", "SpecialChars");
      
      assertThat(result).isEqualTo("Value with 'apostrophes' & symbols < > @");
    }

    @Test
    @DisplayName("Should handle numeric and boolean values")
    void shouldHandleNumericAndBooleanValues() throws IOException {
      LocalizationUtils utils = new LocalizationUtils();
      
      String mixedTypesJson = """
          {
            "en": {
              "NumberValue": 123,
              "BooleanValue": true,
              "StringValue": "text"
            }
          }
          """;
      
      Path mixedTypesFile = tempDir.resolve("mixed-types.json");
      Files.write(mixedTypesFile, mixedTypesJson.getBytes());
      
      String numberResult = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          mixedTypesFile.toString(), "en", "NumberValue");
      String booleanResult = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          mixedTypesFile.toString(), "en", "BooleanValue");
      String stringResult = utils.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          mixedTypesFile.toString(), "en", "StringValue");
      
      assertThat(numberResult).isEqualTo("123");
      assertThat(booleanResult).isEqualTo("true");
      assertThat(stringResult).isEqualTo("text");
    }

    @Test
    @DisplayName("Should work with multiple LocalizationUtils instances independently")
    void shouldWorkWithMultipleInstancesIndependently() throws IOException {
      // Create two different flat JSON files
      String firstJson = """
          {
            "en": {
              "Key": "Value1"
            }
          }
          """;
      
      String secondJson = """
          {
            "en": {
              "Key": "Value2"
            }
          }
          """;
      
      Path firstFile = tempDir.resolve("first.json");
      Path secondFile = tempDir.resolve("second.json");
      Files.write(firstFile, firstJson.getBytes());
      Files.write(secondFile, secondJson.getBytes());
      
      LocalizationUtils utils1 = new LocalizationUtils();
      LocalizationUtils utils2 = new LocalizationUtils();
      
      String result1 = utils1.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          firstFile.toString(), "en", "Key");
      String result2 = utils2.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
          secondFile.toString(), "en", "Key");
      
      assertThat(result1).isEqualTo("Value1");
      assertThat(result2).isEqualTo("Value2");
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work with real-world localization structure")
    void shouldWorkWithRealWorldLocalizationStructure() throws IOException {
      // Create a more realistic localization file
      String realisticJsonContent = """
          {
            "en": {
              "common": {
                "buttons": {
                  "save": "Save",
                  "cancel": "Cancel",
                  "delete": "Delete"
                },
                "messages": {
                  "success": "Operation completed successfully",
                  "error": "An error occurred"
                }
              },
              "pages": {
                "login": {
                  "title": "Login",
                  "username": "Username",
                  "password": "Password"
                }
              }
            },
            "uk": {
              "common": {
                "buttons": {
                  "save": "Зберегти",
                  "cancel": "Скасувати",
                  "delete": "Видалити"
                },
                "messages": {
                  "success": "Операція виконана успішно",
                  "error": "Сталася помилка"
                }
              },
              "pages": {
                "login": {
                  "title": "Вхід",
                  "username": "Ім'я користувача",
                  "password": "Пароль"
                }
              }
            }
          }
          """;
      
      Path realisticFile = tempDir.resolve("realistic.json");
      Files.write(realisticFile, realisticJsonContent.getBytes());
      
      // Test various access patterns
      String saveButtonEn = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          realisticFile.toString(), "en", "save", "common", "buttons");
      assertThat(saveButtonEn).isEqualTo("Save");
      
      String loginTitleUk = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          realisticFile.toString(), "uk", "title", "pages", "login");
      assertThat(loginTitleUk).isEqualTo("Вхід");
      
      List<String> allSaveButtons = localizationUtils.getLocalizationDataForAllLanguages(
          realisticFile.toString(), "save");
      assertThat(allSaveButtons).hasSize(2).contains("\"Save\"", "\"Зберегти\"");
    }

    @Test
    @DisplayName("Should handle multiple method calls on same instance")
    void shouldHandleMultipleMethodCallsOnSameInstance() {
      // Test that the same instance can be used for multiple operations
      JsonNode jsonNode = localizationUtils.uploadLocalizationFile(testJsonFilePath);
      assertThat(jsonNode).isNotNull();
      
      String greeting = localizationUtils.getLocalizationDataForSelectedLanguageByKey(
          testJsonFilePath, "en", "greeting");
      assertThat(greeting).isEqualTo("Hello");
      
      String nestedMessage = localizationUtils.getLocalizationDataForSelectedLanguageByPath(
          testJsonFilePath, "uk", "message", "nested", "level1");
      assertThat(nestedMessage).isEqualTo("Глибоке повідомлення");
      
      List<String> allGreetings = localizationUtils.getLocalizationDataForAllLanguages(
          testJsonFilePath, "greeting");
      assertThat(allGreetings).hasSize(3);
    }
  }
}
