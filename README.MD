# user-login-analyzer
## Задача
Для каждого факта известно имя пользователя, IP адрес с которого осуществлялся вход и время входа.

На основе данного файла требуется найти все IP адреса, с которых происходило больше одного входа за заданное временное окно (например – 1 час).

Результатом должен служить файл в формате CSV со следующими полями:

* IP address – адрес, для которого обнаружен факт множественного входа
* Start – время первого множественного входа в серии
* Stop – время последнего множественного входа в серии
* Users – список имён пользователей и времени входа. Элементы списка разделены запятыми, имя пользователя и время входя разделено двоеточием (':')

 Результат должен содержать все обнаруженные серии множественных входов.

## Запуск
### Входные аргументы:
 * ***--input-file*** - путь до файла с информацией о входах
 * ***--output-file*** - путь до файла с результатами анализа
 * ***--window-size*** - размер окна в формате [scala.concurrent.duration.Duration](https://www.scala-lang.org/api/2.11.12/index.html#scala.concurrent.duration.Duration),
                 например: "1.minute", "1.hour", "2.hours", "1.day"
 * ***--time-zone*** (опциональный, по умолчанию UTC) - временная зона в которой находятся даты из входного файла
 * ***--start-date*** (опциональны, формат: 2015-11-30T23:00:00) - опциональный параметр для указания момента времени с которого начинается первое окно.
                 Если параметр не указан, то берется самое первое время и окна считаются от него.
                Используется в случае если первый логин в коллекции произошел, например в "00:12:11", а окно
                 нужно начать считать с "00:00:00"

### Запуск через sbt
С параметрами по умолчанию
```bash
sbt "run --input-file=/data/logins0.csv --output-file=/data/result1.csv --window-size=1.hour"
```
Со всеми параметрами
```bash
sbt "run --input-file=/data/logins0.csv --output-file=/data/result2.csv --window-size=1.hour --start-date=2015-11-30T23:00:00 --time-zone=Europe/Moscow"
```
### Сборка и запуск fat jar
Сборка jar
```bash
sbt assembly
```
Запуск jar
```bash
java -jar target/scala-2.11/login-analyzer-assembly-0.1.0-SNAPSHOT.jar --input-file=/Users/jaitl/Downloads/logins0.csv --output-file=/Users/jaitl/Downloads/result4.csv --window-size=1.hour
```

## Описание работы программы
### Общий алгоритм работы:
1. Считывается файл с входными данными
2. Данные нарезаются на окна заданного размера
3. Внутри каждого окна выполняется группировка по ip, отфильтровываются записи у которых один вход с одного ip, остаются только ip со множественным входом
4. Результаты сохраняются в файл

### Модели данных:
* [LoginInfo](https://github.com/Jaitl/user-login-analyzer/blob/master/src/main/scala/com/github/jaitl/analyzer/model/LoginInfo.scala) - запись о факте входа
* [WindowLoginInfo](https://github.com/Jaitl/user-login-analyzer/blob/master/src/main/scala/com/github/jaitl/analyzer/model/WindowLoginInfo.scala) - окно, внутри которого находятся факты входа
* [IpLoginSeries](https://github.com/Jaitl/user-login-analyzer/blob/master/src/main/scala/com/github/jaitl/analyzer/model/IpLoginSeries.scala) - информациа о IP, с которых был произведен множественный вход с одного IP в определенном окне

### Сервисы:
* [TimeWindowService](https://github.com/Jaitl/user-login-analyzer/blob/master/src/main/scala/com/github/jaitl/analyzer/service/TimeWindowService.scala) - сервис нарезающий коллекцию с фактами логина LoginInfo на окна по заданному размеру окна.
* [LoginAnalysisService](https://github.com/Jaitl/user-login-analyzer/blob/master/src/main/scala/com/github/jaitl/analyzer/service/LoginAnalysisService.scala) - сервис для анализа количества входов с одного ip за заданное окно.

### Логика запуска
Класс [Main](https://github.com/Jaitl/user-login-analyzer/blob/master/src/main/scala/com/github/jaitl/analyzer/Main.scala) отвечает за
считывание входных аргументов, вызов парсера, запуск логики анализа, вызов логики сохранения, обработку ошибок.
