dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room - tầng Model (Bước 2). Dự án Java nên dùng annotationProcessor,
    // KHÔNG dùng ksp/kapt (2 cái đó chỉ dành cho Kotlin).
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Lifecycle - ViewModel + LiveData, xương sống của MVVM (Bước 3)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // RecyclerView + Fragment (Bước 5)
    implementation(libs.recyclerview)
    implementation(libs.fragment)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
