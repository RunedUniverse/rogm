// FLUSH DATABASE
match (n) detach delete n;
// DEPLOY DATABASE
create (nd:Company {name:"Naughty Dog"})
create (sony:Company {name:"Sony"})
create (e:Person {firstName:"Ellie", fictional:true})
create (j:Person {firstName:"Joel", lastName:"Miller", fictional:true})
create (sj:Artist:Person {firstName:"Shawn", lastName:"James", fictional:false})
create (aj:Actor:Person {firstName:"Ashley", lastName:"Johnson", fictional:false})
create (tb:Artist:Actor:Person {firstName: "Troy", lastName:"Baker", fictional:false})
create (player:Player {_id:"12553411-d527-448d-b82b-33261e4f1618", name:"Testi"})
create (s_1:Song {name: "Through The Valley"})
create (s_2:Song {name:"Midnight Dove"})
create (g1:Game {name:"The Last of Us"})
create (g2:Game {name:"The Last of Us 2"})
create (sj)-[:CREATED]->(s_1)
create (sj)-[:SINGS]->(s_1)
create (sj)-[:CREATED]->(s_2)
create (sj)-[:SINGS]->(s_2)
create (g1)<-[:CREATED]-(nd)
create (g2)<-[:CREATED]-(nd)
create (e)<-[:CHARACTER]-(g1)
create (e)<-[:CHARACTER]-(g2)
create (j)<-[:CHARACTER]-(g1)
create (j)<-[:CHARACTER]-(g2)
create (t:Video)<-[:TRAILER]-(g2)
create (s_1)<-[:CONTAINS]-(t)
create (e)<-[:CONTAINS]-(t)
create (aj)-[:PLAYS]->(e)
create (tb)-[:PLAYS]->(j)
create (e)-[:SINGS]->(s_1)
create (nd)-[:OWNED_BY]->(sony)
;