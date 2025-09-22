package com.example.failedeventsprocessor.mapper;


import com.example.failedeventsprocessor.dto.response.FileResponse;
import org.mapstruct.Mapper;

@Mapper
public interface FileMapper {

    FileResponse toFileResponse(String url, String key);
}
