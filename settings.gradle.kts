rootProject.name = "ai-platform"

include("libs:common-core")
include("libs:common-infrastructure")
include("libs:common-outbox")
include("libs:common-saga")

include("service:datahub")
include("service:datarex")
include("service:vecdash")
