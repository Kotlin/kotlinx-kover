/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("java-platform")
    id("maven-publish")
}

// Android

val appcompat = "1.4.0"
val constraintLayout = "2.1.4"
val coreKtx = "1.8.0"
val fragmentKtx = "1.5.2"
val lifecycleLiveDataKtx = "2.5.1"
val material = "1.6.1"


// Tests

val archCoreTesting = "2.1.0" // TODO NINO How to remove mockito dep ?


// Unit tests

val coroutinesTest = "1.6.4"
val junit = "4.13.2"
val mockk = "1.12.7"


dependencies {
    constraints {
        // Android

        api("${Libs.APPCOMPAT}:$appcompat")
        api("${Libs.CONSTRAINT_LAYOUT}:$constraintLayout")
        api("${Libs.CORE_KTX}:$coreKtx")
        api("${Libs.FRAGMENT_KTX}:$fragmentKtx")
        api("${Libs.LIFECYCLE_LIVE_DATA_KTX}:$lifecycleLiveDataKtx")
        api("${Libs.MATERIAL}:$material")


        // Tests

        api("${Libs.ARCH_CORE_TESTING}:$archCoreTesting")


        // Unit tests

        api("${Libs.COROUTINES_TEST}:$coroutinesTest")
        api("${Libs.JUNIT}:$junit")
        api("${Libs.MOCKK}:$mockk")
    }
}

publishing {
    publications {
        create<MavenPublication>("myPlatform") {
            from(components["javaPlatform"])
        }
    }
}
