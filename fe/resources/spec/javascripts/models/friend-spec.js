describe ("Friend" , function() {
    
    it("should return proper facebook image url", function(){
        friend = new Friend({"id": "1000"});
        expect(friend.get('fbSmallImageUrl')).toBe("//graph.facebook.com/1000/picture?type=small");
        expect(friend.get('fbLargeImageUrl')).toBe("//graph.facebook.com/1000/picture?type=large");
    });

});

