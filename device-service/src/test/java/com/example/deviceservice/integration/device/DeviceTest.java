package com.example.deviceservice.integration.device;

import com.example.deviceservice.dto.request.DeviceRequest;
import com.example.deviceservice.dto.response.DeviceResponse;
import com.example.deviceservice.dto.response.ErrorResponse;
import com.example.deviceservice.integration.config.DeviceServiceConfiguration;
import com.example.deviceservice.model.Device;
import com.example.deviceservice.utils.JwtUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.example.deviceservice.utils.DeviceData.DEVICE_LAPTOP_REQUEST;
import static com.example.deviceservice.utils.DeviceData.DEVICE_PHONE_REQUEST;
import static com.example.deviceservice.utils.DeviceData.LAPTOP_DEVICE_ID;
import static com.example.deviceservice.utils.DeviceData.PHONE_DEVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class DeviceTest extends DeviceServiceConfiguration {

    private final static String BASE_DEVICE_V1_ENDPOINT = "/v1/devices";
    private final static String CREATE_DEVICE_V1_ENDPOINT = BASE_DEVICE_V1_ENDPOINT;
    private final static String GET_DEVICE_V1_ENDPOINT = "%s/%s".formatted(BASE_DEVICE_V1_ENDPOINT, "{id}");
    private final static String DELETE_DEVICE_V1_ENDPOINT = "%s/%s".formatted(BASE_DEVICE_V1_ENDPOINT, "{id}");

    private final static String NOT_FOUND_ERROR_MESSAGE = "Device with id = %s not found";

    private static final String GET_DEVICES = """
            SELECT *
                FROM devices
           WHERE device_id = ?
           """;

    private static final String EXISTS_DEVICES = """
            SELECT EXISTS(
                SELECT 1
                FROM devices
                WHERE device_id = ?
            )
           """;

    private static final String COUNT_DEVICES = """
             SELECT count(*)
                FROM devices
             WHERE device_id = ?
           """;

    private static String bearerAuthorizationHeader;

    @BeforeAll
    public static void beforeAll() throws URISyntaxException {
        bearerAuthorizationHeader = JwtUtils.getBearerAuthorizationHeader(
                getAuthOpenIdConnectTokenUrl(), KEYCLOAK_CLIENT_ID, KEYCLOAK_USER, KEYCLOAK_PASSWORD
        );
    }

    @Test
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_successfulDeviceShardingToMaster0() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(CREATE_DEVICE_V1_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DEVICE_PHONE_REQUEST))
                        .header(AUTHORIZATION, bearerAuthorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        int expectedShard = Math.abs(PHONE_DEVICE_ID.hashCode()) % 2;
        assertEquals(0, expectedShard);

        Device savedDeviceFromShard0 = shard0JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {PHONE_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedDeviceFromShard0);
        assertionsDeviceRequestByDevice(DEVICE_PHONE_REQUEST, savedDeviceFromShard0);

        Boolean savedDeviceFromShard1 = shard1JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {PHONE_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);

        DeviceResponse deviceResponse = deserializeResponse(DeviceResponse.class, mvcResult);
        assertionsDeviceResponseByRequest(DEVICE_PHONE_REQUEST, deviceResponse);

        Integer savedDeviceCount = shard0JdbcTemplate.queryForObject(COUNT_DEVICES, new Object[] {PHONE_DEVICE_ID}, Integer.class);
        assertEquals(1, savedDeviceCount);
    }

    @Test
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_successfulDeviceShardingToMaster1() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(CREATE_DEVICE_V1_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DEVICE_LAPTOP_REQUEST))
                        .header(AUTHORIZATION, bearerAuthorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        int expectedShard = Math.abs(LAPTOP_DEVICE_ID.hashCode()) % 2;
        assertEquals(1, expectedShard);

        Device savedDeviceFromShard0 = shard1JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedDeviceFromShard0);
        assertionsDeviceRequestByDevice(DEVICE_LAPTOP_REQUEST, savedDeviceFromShard0);

        Boolean savedDeviceFromShard1 = shard0JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);

        DeviceResponse deviceResponse = deserializeResponse(DeviceResponse.class, mvcResult);
        assertionsDeviceResponseByRequest(DEVICE_LAPTOP_REQUEST, deviceResponse);

        Integer savedDeviceCount = shard1JdbcTemplate.queryForObject(COUNT_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Integer.class);
        assertEquals(1, savedDeviceCount);
    }

    @Test
    void device_deviceProcessing_createNotAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_DEVICE_V1_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(DEVICE_LAPTOP_REQUEST)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        int expectedShard = Math.abs(LAPTOP_DEVICE_ID.hashCode()) % 2;
        assertEquals(1, expectedShard);

        Boolean savedDeviceFromShard0 = shard1JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard0);

        Boolean savedDeviceFromShard1 = shard0JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);
    }

    @Test
    @Sql("classpath:db/populate_devices.sql")
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_GetDeviceSuccessful() throws Exception {

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(GET_DEVICE_V1_ENDPOINT, PHONE_DEVICE_ID)
                        .header(AUTHORIZATION, bearerAuthorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        DeviceResponse deviceResponse = deserializeResponse(DeviceResponse.class, mvcResult);
        assertionsDeviceResponseByRequest(DEVICE_PHONE_REQUEST, deviceResponse);
    }

    @Test
    void device_deviceProcessing_GetDeviceNotFound() throws Exception {
        int expectedShard = Math.abs(PHONE_DEVICE_ID.hashCode()) % 2;
        assertEquals(0, expectedShard);

        Boolean savedDeviceFromShard0 = shard0JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {PHONE_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard0);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(GET_DEVICE_V1_ENDPOINT, PHONE_DEVICE_ID)
                        .header(AUTHORIZATION, bearerAuthorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = deserializeResponse(ErrorResponse.class, mvcResult);
        assertEquals(errorResponse.status(), NOT_FOUND.value());
        assertEquals(errorResponse.title(), NOT_FOUND_ERROR_MESSAGE.formatted(PHONE_DEVICE_ID));
        assertEquals(errorResponse.type(), BASE_DEVICE_V1_ENDPOINT + "/" + PHONE_DEVICE_ID);
    }

    @Test
    void device_deviceProcessing_GetDeviceNotAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(GET_DEVICE_V1_ENDPOINT, PHONE_DEVICE_ID))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    @Sql("classpath:db/populate_devices.sql")
    @Sql(value = "classpath:db/clear-devices.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void device_deviceProcessing_DeleteDeviceSuccessful() throws Exception {
        int expectedShard = Math.abs(PHONE_DEVICE_ID.hashCode()) % 2;
        assertEquals(0, expectedShard);

        Device savedDeviceFromShard0 = shard0JdbcTemplate.queryForObject(GET_DEVICES, new Object[] {PHONE_DEVICE_ID}, BeanPropertyRowMapper.newInstance(Device.class));
        assertNotNull(savedDeviceFromShard0);
        assertionsDeviceRequestByDevice(DEVICE_PHONE_REQUEST, savedDeviceFromShard0);

        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_DEVICE_V1_ENDPOINT, PHONE_DEVICE_ID)
                        .header(AUTHORIZATION, bearerAuthorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();

        Boolean savedDeviceFromShard1 = shard0JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {PHONE_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);
    }

    @Test
    void device_deviceProcessing_DeleteDeviceNotFound() throws Exception {
        int expectedShard = Math.abs(LAPTOP_DEVICE_ID.hashCode()) % 2;
        assertEquals(1, expectedShard);

        Boolean savedDeviceFromShard1 = shard1JdbcTemplate.queryForObject(EXISTS_DEVICES, new Object[] {LAPTOP_DEVICE_ID}, Boolean.class);
        assertFalse(savedDeviceFromShard1);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_DEVICE_V1_ENDPOINT, LAPTOP_DEVICE_ID)
                        .header(AUTHORIZATION, bearerAuthorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();


        ErrorResponse errorResponse = deserializeResponse(ErrorResponse.class, mvcResult);
        assertEquals(errorResponse.status(), NOT_FOUND.value());
        assertEquals(errorResponse.title(), NOT_FOUND_ERROR_MESSAGE.formatted(LAPTOP_DEVICE_ID));
    }

    @Test
    void device_deviceProcessing_DeleteDeviceNotAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DELETE_DEVICE_V1_ENDPOINT, PHONE_DEVICE_ID))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    private boolean assertionsDeviceRequestByDevice(DeviceRequest deviceRequest, Device device) {
        return deviceRequest.id().equals(device.getId())
                && deviceRequest.deviceType().equals(device.getDeviceType())
                && deviceRequest.createdAt().equals(device.getCreatedAt())
                && deviceRequest.meta().equals(device.getMeta());
    }

    private boolean assertionsDeviceResponseByRequest(DeviceRequest deviceRequest, DeviceResponse deviceResponse) {
        return deviceRequest.id().equals(deviceResponse.id())
                && deviceRequest.deviceType().equals(deviceResponse.deviceType())
                && deviceRequest.createdAt().equals(deviceResponse.createdAt())
                && deviceRequest.meta().equals(deviceResponse.meta());
    }

    private<T> T deserializeResponse(Class<T> clazz, MvcResult mvcResult) throws IOException {
        return objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), clazz);
    }
}
