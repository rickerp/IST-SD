# Demo

## Setup
1. Install as shown in [../README.md](../README.md).

2. Execute `silo-server` as shown in [../silo-server/README.md](../silo-server/README.md).

**Example**
```sh 
$ silo-server 8080
```

3. Execute `eye` as shown in [../eye/README.md](../eye/README.md) for a certain input found in this directory.

**Example**
```sh
$ eye localhost 8080 Taguspark -50 50 < input01.txt
```

4. Execute `spotter` as shown in [../spotter/README.md](../spotter/README.md).
   
**Example**
```sh
$ spotter localhost 8080
```

# Examples

```sh
$ silo-server 8080
```

### **track person**

```sh
$ eye localhost 8080 Taguspark -50 50 < input01.txt
$ spotter localhost 8080
> spot person 123
person,123,2020-04-03T10:10:31,Taguspark,-50.0,50.0
> clear
> [ctrl-d]
```

### **track car**
```sh
$ eye localhost 8080 Taguspark -50 50 < input01.txt
$ spotter localhost 8080
> spot car AABB77
car,AABB77,2020-04-03T09:36:03,Taguspark,-50.0,50.0
> clear
> [ctrl-d]
```

### **trackMatch**
```sh
$ eye localhost 8080 Taguspark -50 50 < input04.txt
$ spotter localhost 8080
> spot person 1*
person,123,2020-04-03T09:58:57,Taguspark,-50.0,50.0
person,189,2020-04-03T09:58:57,Taguspark,-50.0,50.0
> clear
> [ctrl-d]
```

### **ping**
```sh
$ eye localhost 8080 Taguspark -50 50 < input07.txt
$ spotter localhost 8080
> ping
Server is running
> clear
> [ctrl-d]
```

### **clear**
```sh
$ eye localhost 8080 Taguspark -50 50 < input01.txt
$ spotter localhost 8080
> spot car AABB77
car,AABB77,2020-04-03T10:00:14,Taguspark,-50.0,50.0
> clear
> spot car AABB77

> [ctrl-d]
```

### **trace**
```sh
$ eye localhost 8080 Taguspark -50 50 < input03.txt
$ spotter localhost 8080
> trail person 123
person,123,2020-04-03T10:01:43,Taguspark,-50.0,50.0
person,123,2020-04-03T10:01:43,Taguspark,-50.0,50.0
person,123,2020-04-03T10:01:43,Taguspark,-50.0,50.0
> clear
> [ctrl-d]
```

### **invalid person id**
```
$ eye localhost 8080 Taguspark -50 50 < input05.txt
Report failed: Person's id must be a number.
$ spotter localhost 8080
> clear
> [ctrl-d]
```

### **invalid car plate**
```
$ eye localhost 8080 Taguspark -50 50 < input06.txt
Report failed: Plate has invalid formatting.
$ spotter localhost 8080
> clear
> [ctrl-d]
```

### **multiple eyes**
```
$ eye localhost 8080 Taguspark -50 50 < input01.txt
$ eye localhost 8080 Alameda -10 10 < input01.txt
$ spotter localhost 8080
> trail person 123
person,123,2020-04-03T10:07:25,Alameda,-10.0,10.0
person,123,2020-04-03T10:07:22,Taguspark,-50.0,50.0
> clear
> [ctrl-d]
```
