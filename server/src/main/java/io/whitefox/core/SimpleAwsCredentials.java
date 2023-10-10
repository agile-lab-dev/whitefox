package io.whitefox.core;

public record SimpleAwsCredentials(
        String awsAccessKeyId,
        String awsSecretAccessKey,
        String region
) implements AwsCredentials {
}
