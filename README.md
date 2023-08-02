# Lake Sharing: An Open Protocol for Secure Data Sharing

Fork of Delta-Sharing with the following goals:

- allow to share data in different table formats (iceberg, hudi, hive-style) leveraging the delta-sharing protocol and adding new APIs where needed
- lake-sharing protocol will cover not only data sharing needs but also administration needs, such as creation of shares, configuration of sources, etc.
- provide a production-grade server implementation of both the original delta-sharing protocol and the new lake-sharing protocol

This repository contains:

- the original delta-sharing protocol
- the lake-sharing protocol
- server implementation of both protocols
- lake-sharing clients