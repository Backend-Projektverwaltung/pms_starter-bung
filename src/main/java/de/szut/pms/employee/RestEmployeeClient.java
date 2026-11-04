package de.szut.pms.employee;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestEmployeeClient implements EmployeeClient {

    private final RestClient restClient;

    public RestEmployeeClient(RestClient employeeRestClient) {
        this.restClient = employeeRestClient;
    }

    @Override
    public EmployeeResponse findById(Long employeeId) {
        try {
            EmployeeResponse employee = restClient.get()
                    .uri("/employees/{id}", employeeId)
                    .retrieve()
                    .body(EmployeeResponse.class);
            if (employee == null) {
                throw new EmployeeServiceException(
                        "Der Employee-Service hat eine ungültige Antwort geliefert.",
                        HttpStatus.BAD_GATEWAY,
                        null);
            }
            return employee;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new EmployeeNotFoundException(employeeId);
        } catch (ResourceAccessException exception) {
            throw new EmployeeServiceException(
                    "Der Employee-Service ist nicht erreichbar.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    exception);
        } catch (RestClientResponseException exception) {
            throw new EmployeeServiceException(
                    "Die Anfrage an den Employee-Service ist fehlgeschlagen.",
                    HttpStatus.BAD_GATEWAY,
                    exception);
        }
    }
}
