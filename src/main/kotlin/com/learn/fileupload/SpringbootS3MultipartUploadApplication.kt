package com.learn.fileupload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
@EnableConfigurationProperties
class SpringbootS3MultipartUploadApplication

fun main(args: Array<String>) {
	runApplication<SpringbootS3MultipartUploadApplication>(*args)
}
