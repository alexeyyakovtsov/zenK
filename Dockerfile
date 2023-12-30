# Используем базовый образ с JDK
FROM openjdk:11-jre-slim as builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы Gradle Wrapper
COPY gradlew .
COPY gradle gradle

# Копируем файлы с зависимостями
COPY build.gradle .
COPY settings.gradle .

# Выполняем сборку, обновляем зависимости
RUN ./gradlew build --no-daemon

# Копируем остальные файлы проекта
COPY . .

# Снова выполняем сборку для финального JAR
RUN ./gradlew build --no-daemon

# Отдельный этап для минимизации образа
FROM openjdk:11-jre-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл из предыдущего этапа
COPY --from=builder /app/build/libs/backend-*.jar ./application.jar

# Команда для запуска приложения
CMD java -server -Xmx256M -jar application.jar