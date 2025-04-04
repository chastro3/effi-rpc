rootProject.name = "effi-rpc"
include("effi-rpc-common")
include("effi-rpc-bom")
include("effi-rpc-contract")
include("effi-rpc-governance")
include("effi-rpc-registry")
include("effi-rpc-registry:effi-rpc-registry-api")
findProject(":effi-rpc-registry:effi-rpc-registry-api")?.name = "effi-rpc-registry-api"
include("effi-rpc-registry:effi-rpc-registry-consul")
findProject(":effi-rpc-registry:effi-rpc-registry-consul")?.name = "effi-rpc-registry-consul"
include("effi-rpc-registry:effi-rpc-registry-nacos")
findProject(":effi-rpc-registry:effi-rpc-registry-nacos")?.name = "effi-rpc-registry-nacos"
include("effi-rpc-proxy")
include("effi-rpc-proxy:effi-rpc-proxy-api")
findProject(":effi-rpc-proxy:effi-rpc-proxy-api")?.name = "effi-rpc-proxy-api"
include("effi-rpc-proxy:effi-rpc-proxy-jdk")
findProject(":effi-rpc-proxy:effi-rpc-proxy-jdk")?.name = "effi-rpc-proxy-jdk"
include("effi-rpc-proxy:effi-rpc-proxy-cglib")
findProject(":effi-rpc-proxy:effi-rpc-proxy-cglib")?.name = "effi-rpc-proxy-cglib"
include("effi-rpc-proxy:effi-rpc-proxy-bytebuddy")
findProject(":effi-rpc-proxy:effi-rpc-proxy-bytebuddy")?.name = "effi-rpc-proxy-bytebuddy"
include("effi-rpc-serialization")
include("effi-rpc-serialization:effi-rpc-serialization-api")
findProject(":effi-rpc-serialization:effi-rpc-serialization-api")?.name = "effi-rpc-serialization-api"
include("effi-rpc-serialization:effi-rpc-serialization-jdk")
findProject(":effi-rpc-serialization:effi-rpc-serialization-jdk")?.name = "effi-rpc-serialization-jdk"
include("effi-rpc-serialization:effi-rpc-serialization-json")
findProject(":effi-rpc-serialization:effi-rpc-serialization-json")?.name = "effi-rpc-serialization-json"
include("effi-rpc-serialization:effi-rpc-serialization-kryo")
findProject(":effi-rpc-serialization:effi-rpc-serialization-kryo")?.name = "effi-rpc-serialization-kryo"
include("effi-rpc-serialization:effi-rpc-serialization-msgpack")
findProject(":effi-rpc-serialization:effi-rpc-serialization-msgpack")?.name = "effi-rpc-serialization-msgpack"
include("effi-rpc-serialization:effi-rpc-serialization-protobuf")
findProject(":effi-rpc-serialization:effi-rpc-serialization-protobuf")?.name = "effi-rpc-serialization-protobuf"
include("effi-rpc-metrics")
include("effi-rpc-engine")
include("effi-rpc-engine:effi-rpc-engine-api")
findProject(":effi-rpc-engine:effi-rpc-engine-api")?.name = "effi-rpc-engine-api"
include("effi-rpc-engine:effi-rpc-engine-http")
findProject(":effi-rpc-engine:effi-rpc-engine-http")?.name = "effi-rpc-engine-http"
include("effi-rpc-test")
include("effi-rpc-demo")
include("effi-rpc-demo:consumer")
findProject(":effi-rpc-demo:consumer")?.name = "consumer"
include("effi-rpc-demo:provider")
findProject(":effi-rpc-demo:provider")?.name = "provider"
include("effi-rpc-transport")
include("effi-rpc-transport:effi-rpc-transport-api")
findProject(":effi-rpc-transport:effi-rpc-transport-api")?.name = "effi-rpc-transport-api"
include("effi-rpc-transport:effi-rpc-transport-netty")
findProject(":effi-rpc-transport:effi-rpc-transport-netty")?.name = "effi-rpc-transport-netty"
